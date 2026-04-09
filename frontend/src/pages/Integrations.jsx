import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { ArrowLeft, Save, ExternalLink, Plus, Trash2, RefreshCw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Integrations() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [metaAccounts, setMetaAccounts] = useState([]);
  const [shopifyStores, setShopifyStores] = useState([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);

  const [newMeta, setNewMeta] = useState({ accountName: '', adAccountId: '', accessToken: '', currency: 'USD' });
  const [newShopify, setNewShopify] = useState({ storeName: '', storeUrl: '', clientId: '', clientSecret: '' });

  const fetchData = async () => {
    try {
      const [metaRes, shopifyRes] = await Promise.all([
        api.get('/integrations/meta'),
        api.get('/integrations/shopify')
      ]);
      setMetaAccounts(metaRes.data || []);
      setShopifyStores(shopifyRes.data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleAddMeta = async (e) => {
    e.preventDefault();
    try {
      await api.post('/integrations/meta', newMeta);
      setNewMeta({ accountName: '', adAccountId: '', accessToken: '', currency: 'USD' });
      fetchData();
    } catch (e) {
      alert("Failed to add Meta account");
    }
  };

  const handleAddShopify = async (e) => {
    e.preventDefault();
    try {
      await api.post('/integrations/shopify', newShopify);
      setNewShopify({ storeName: '', storeUrl: '', clientId: '', clientSecret: '' });
      fetchData();
    } catch (e) {
      alert("Failed to add Shopify store");
    }
  };

  const handleSync = async () => {
    try {
      setSyncing(true);
      await api.post('/etl/trigger');
      alert("Sync completed successfully!");
      // Optionally fetch data if metrics are shown here
    } catch (e) {
      alert(e.response?.data || "Sync Failed");
    } finally {
      setSyncing(false);
    }
  };

  const handleDeleteMeta = async (id) => {
    if(confirm("Delete this Meta account?")) {
      await api.delete(`/integrations/meta/${id}`);
      fetchData();
    }
  };

  const handleDeleteShopify = async (id) => {
    if(confirm("Delete this Shopify store?")) {
      await api.delete(`/integrations/shopify/${id}`);
      fetchData();
    }
  };

  return (
    <div style={{ padding: '32px', maxWidth: '1000px', margin: '0 auto' }}>
      <header className="flex items-center justify-between gap-4 mb-6">
        <div className="flex items-center gap-4">
          <button 
            onClick={() => navigate('/')} 
            style={{ backgroundColor: 'var(--bg-card)', border: '1px solid var(--border-color)', color: 'var(--text-main)', padding: '8px 12px' }}
          >
            <ArrowLeft size={20} />
          </button>
          <div>
            <h1 style={{ fontSize: '32px', fontWeight: 'bold' }}>Integrations</h1>
            <p style={{ color: 'var(--text-muted)' }}>Manage your automated data sources (Meta Ads & Shopify)</p>
          </div>
        </div>
        <button onClick={handleSync} disabled={syncing} className="flex items-center gap-2" style={{ backgroundColor: 'var(--primary)', color: 'white', padding: '10px 16px', borderRadius: '8px' }}>
          <RefreshCw size={18} className={syncing ? 'animate-spin' : ''} />
          {syncing ? 'Syncing...' : 'Sync Now'}
        </button>
      </header>

      {loading ? (
          <div className="flex justify-center my-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary" style={{ borderRightColor: 'transparent', borderRadius: '50%', border: '4px solid', animation: 'spin 1s linear infinite' }}></div>
          </div>
      ) : (
        <div className="flex flex-col gap-8">
          {/* META SECTION */}
          <section className="card">
            <div className="flex items-center gap-3 mb-6">
              <div style={{ padding: '8px', borderRadius: '8px', backgroundColor: '#1877f220', color: '#1877f2' }}>
                 <ExternalLink size={24} />
              </div>
              <h2 style={{ fontSize: '24px', fontWeight: 'bold' }}>Meta Ad Accounts</h2>
            </div>
            
            {metaAccounts.length > 0 && (
              <div className="mb-6 grid gap-4 grid-cols-2">
                {metaAccounts.map((meta, i) => (
                  <div key={i} style={{ padding: '16px', backgroundColor: 'var(--bg-dark)', borderRadius: '8px', border: '1px solid var(--border-color)', position: 'relative' }}>
                    <div className="font-bold">{meta.accountName || "Unnamed Account"}</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '13px' }}>ID: {meta.adAccountId}</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Currency: {meta.currency}</div>
                    <button onClick={() => handleDeleteMeta(meta.id)} style={{ position: 'absolute', top: '16px', right: '16px', color: 'var(--danger)', background: 'transparent', padding: '4px' }}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
              </div>
            )}

            <form onSubmit={handleAddMeta} style={{ borderTop: '1px solid var(--border-color)', paddingTop: '24px' }}>
              <h3 className="mb-4" style={{ fontSize: '18px', fontWeight: '600' }}>Connect New Ad Account</h3>
              <div className="grid gap-4" style={{ gridTemplateColumns: '1fr 1fr' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Account Reference (e.g., FABELLA or SR)</label>
                  <input type="text" required value={newMeta.accountName} onChange={e => setNewMeta({...newMeta, accountName: e.target.value})} placeholder="FABELLA" />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Ad Account ID</label>
                  <input type="text" required value={newMeta.adAccountId} onChange={e => setNewMeta({...newMeta, adAccountId: e.target.value})} placeholder="act_123456789" />
                </div>
                <div style={{ gridColumn: '1 / -1' }}>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Permanent System User Access Token</label>
                  <input type="password" required value={newMeta.accessToken} onChange={e => setNewMeta({...newMeta, accessToken: e.target.value})} placeholder="EAAB..." />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Currency</label>
                  <select value={newMeta.currency} onChange={e => setNewMeta({...newMeta, currency: e.target.value})}>
                    <option value="USD">USD</option>
                    <option value="INR">INR</option>
                    <option value="EUR">EUR</option>
                  </select>
                </div>
              </div>
              <button type="submit" className="flex items-center gap-2 mt-6">
                <Plus size={18} /> Connect Meta Account
              </button>
            </form>
          </section>

          {/* SHOPIFY SECTION */}
          <section className="card">
            <div className="flex items-center gap-3 mb-6">
              <div style={{ padding: '8px', borderRadius: '8px', backgroundColor: '#95bf4720', color: '#95bf47' }}>
                 <ExternalLink size={24} />
              </div>
              <h2 style={{ fontSize: '24px', fontWeight: 'bold' }}>Shopify Stores</h2>
            </div>
            
            {shopifyStores.length > 0 && (
              <div className="mb-6 grid gap-4 grid-cols-2">
                {shopifyStores.map((shop, i) => (
                  <div key={i} style={{ padding: '16px', backgroundColor: 'var(--bg-dark)', borderRadius: '8px', border: '1px solid var(--border-color)', position: 'relative' }}>
                    <div className="font-bold">{shop.storeName}</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '13px' }}>URL: {shop.storeUrl}</div>
                    <button onClick={() => handleDeleteShopify(shop.id)} style={{ position: 'absolute', top: '16px', right: '16px', color: 'var(--danger)', background: 'transparent', padding: '4px' }}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
              </div>
            )}

            <form onSubmit={handleAddShopify} style={{ borderTop: '1px solid var(--border-color)', paddingTop: '24px' }}>
              <h3 className="mb-4" style={{ fontSize: '18px', fontWeight: '600' }}>Connect New Store</h3>
              <div className="grid gap-4" style={{ gridTemplateColumns: '1fr 1fr' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Store Name (e.g., FABELLA)</label>
                  <input type="text" required value={newShopify.storeName} onChange={e => setNewShopify({...newShopify, storeName: e.target.value})} placeholder="My Store" />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Store Prefix</label>
                  <input type="text" required value={newShopify.storeUrl} onChange={e => setNewShopify({...newShopify, storeUrl: e.target.value})} placeholder="fabella" />
                  <small style={{ color: 'var(--text-muted)', marginTop: '4px', display: 'block' }}>Just the prefix before .myshopify.com</small>
                </div>
                <div style={{ gridColumn: '1 / -1' }}>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Client ID</label>
                  <input type="text" required value={newShopify.clientId} onChange={e => setNewShopify({...newShopify, clientId: e.target.value})} placeholder="07d323157b3e..." />
                </div>
                <div style={{ gridColumn: '1 / -1' }}>
                  <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)', fontSize: '14px' }}>Client Secret</label>
                  <input type="password" required value={newShopify.clientSecret} onChange={e => setNewShopify({...newShopify, clientSecret: e.target.value})} placeholder="shpss_..." />
                </div>
              </div>
              <button type="submit" className="flex items-center gap-2 mt-6">
                <Plus size={18} /> Connect Shopify Store
              </button>
            </form>
          </section>
        </div>
      )}
    </div>
  );
}
