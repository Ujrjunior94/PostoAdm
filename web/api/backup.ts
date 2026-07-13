import type { VercelRequest, VercelResponse } from '@vercel/node';
import { createClient } from '@supabase/supabase-js';
import { kv } from '@vercel/kv';

// Volatile in-memory store for instant zero-configuration testing.
// Note: In serverless environments, this memory is cleared when the function container recycles.
const volatileMemoryStore: Record<string, any> = {};

// Helper to write standard CORS headers
function setCorsHeaders(res: VercelResponse) {
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');
}

export default async function handler(req: VercelRequest, res: VercelResponse) {
  // Handle CORS preflight request
  if (req.method === 'OPTIONS') {
    setCorsHeaders(res);
    return res.status(200).end();
  }

  setCorsHeaders(res);

  const supabaseUrl = process.env.SUPABASE_URL;
  const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_KEY;
  const useSupabase = !!(supabaseUrl && supabaseKey);

  const kvUrl = process.env.KV_REST_API_URL;
  const kvToken = process.env.KV_REST_API_TOKEN;
  const useKv = !!(kvUrl && kvToken);

  const cnpj = (req.query.cnpj as string || req.query.id as string || '').trim();

  // --- GET METHOD: Retrieve backup ---
  if (req.method === 'GET') {
    if (!cnpj) {
      return res.status(400).json({ 
        error: "CNPJ/ID do posto é obrigatório na query (?cnpj=...)" 
      });
    }

    try {
      // 1. Try Vercel KV if configured
      if (useKv) {
        console.log(`[Vercel KV] Buscando backup para CNPJ: ${cnpj}`);
        const backup = await kv.get(`backup:${cnpj}`);
        if (backup) {
          return res.status(200).json(typeof backup === 'string' ? JSON.parse(backup) : backup);
        }
      }

      // 2. Try Supabase if configured and Vercel KV didn't return
      if (useSupabase) {
        console.log(`[Supabase] Buscando backup para CNPJ: ${cnpj}`);
        const supabase = createClient(supabaseUrl!, supabaseKey!);
        const { data, error } = await supabase
          .from('posto_backups')
          .select('data')
          .eq('id', cnpj)
          .single();

        if (error && error.code !== 'PGRST116') { // PGRST116 is code for 0 rows returned
          console.error('[Supabase Error]', error);
        } else if (data && data.data) {
          return res.status(200).json(data.data);
        }
      }

      // 3. Try Volatile In-Memory Fallback
      if (volatileMemoryStore[cnpj]) {
        console.log(`[In-Memory] Buscando backup para CNPJ: ${cnpj}`);
        return res.status(200).json(volatileMemoryStore[cnpj]);
      }

      // If no data found at all
      return res.status(200).json({ 
        message: "Nenhum backup encontrado na nuvem para este CNPJ",
        cnpj: cnpj,
        engine: useKv ? "Vercel KV" : useSupabase ? "Supabase" : "In-Memory (Volátil)",
        data: null 
      });

    } catch (err: any) {
      console.error("Erro ao buscar backup:", err);
      return res.status(500).json({ error: "Erro interno do servidor", details: err.message });
    }
  }

  // --- POST METHOD: Save backup ---
  if (req.method === 'POST') {
    const body = req.body;
    if (!body) {
      return res.status(400).json({ error: "Corpo da requisição JSON é obrigatório." });
    }

    // Extract CNPJ & backup content
    const targetCnpj = (body.cnpj || body.id || '').trim();
    const backupContent = body.data;

    if (!targetCnpj) {
      return res.status(400).json({ error: "CNPJ ou ID do posto é obrigatório no corpo do JSON (campo 'cnpj' ou 'id')." });
    }
    if (!backupContent) {
      return res.status(400).json({ error: "O campo 'data' contendo o JSON de backup do posto é obrigatório." });
    }

    try {
      let savedInKv = false;
      let savedInSupabase = false;

      // 1. Save to Vercel KV if configured
      if (useKv) {
        console.log(`[Vercel KV] Salvando backup para CNPJ: ${targetCnpj}`);
        await kv.set(`backup:${targetCnpj}`, JSON.stringify(backupContent));
        savedInKv = true;
      }

      // 2. Save to Supabase if configured
      if (useSupabase) {
        console.log(`[Supabase] Salvando backup para CNPJ: ${targetCnpj}`);
        const supabase = createClient(supabaseUrl!, supabaseKey!);
        const { error } = await supabase
          .from('posto_backups')
          .upsert({ 
            id: targetCnpj, 
            data: backupContent, 
            updated_at: new Date().toISOString() 
          });

        if (error) {
          console.error('[Supabase Error]', error);
          if (!savedInKv) {
            return res.status(500).json({ error: "Erro ao salvar no Supabase", details: error.message });
          }
        } else {
          savedInSupabase = true;
        }
      }

      // Always update volatile in-memory cache as fallback
      volatileMemoryStore[targetCnpj] = backupContent;

      return res.status(200).json({
        success: true,
        message: "Backup sincronizado com sucesso!",
        cnpj: targetCnpj,
        timestamp: new Date().toISOString(),
        engines: {
          vercel_kv: savedInKv ? "Sucesso" : "Não configurado",
          supabase: savedInSupabase ? "Sucesso" : "Não configurado",
          in_memory: "Sucesso (Local do contêiner)"
        }
      });

    } catch (err: any) {
      console.error("Erro ao salvar backup:", err);
      return res.status(500).json({ error: "Erro interno ao processar sincronização", details: err.message });
    }
  }

  // Unhandled HTTP method
  return res.status(405).json({ error: `Método ${req.method} não permitido` });
}
