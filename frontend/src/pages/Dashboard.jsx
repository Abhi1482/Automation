import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, Legend } from 'recharts';
import { ArrowUpRight, DollarSign, Target, Activity, Calendar, Filter, Settings } from 'lucide-react';
import api from '../services/api';

export default function Dashboard() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [metrics, setMetrics] = useState([]);
    const [kpi, setKpi] = useState({ totalSpend: 0, totalRevenue: 0, totalProfit: 0, avgCAC: 0, avgROAS: 0 });
    const [loading, setLoading] = useState(true);
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
    const [channel, setChannel] = useState('');

    const fetchData = async () => {
        setLoading(true);
        try {
            const [metricsRes, kpiRes] = await Promise.all([
                api.get(`/metrics/hourly?date=${date}&channel=${channel}`),
                api.get('/metrics/kpi')
            ]);
            setMetrics(metricsRes.data || []);
            setKpi(kpiRes.data || {});
        } catch (error) {
            console.error("Failed to load dashboard data", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
        const interval = setInterval(fetchData, 300000); // 5 min auto refresh
        return () => clearInterval(interval);
    }, [date, channel]);

    const KpiCard = ({ title, value, icon: Icon, colorClass }) => (
        <div className="card">
            <div className="flex justify-between items-center mb-4">
                <h3 style={{ color: 'var(--text-muted)' }}>{title}</h3>
                <div style={{ padding: '8px', borderRadius: '8px', backgroundColor: 'var(--bg-hover)' }}>
                    <Icon className={colorClass} size={20} style={{ color: 'var(--text-main)' }} />
                </div>
            </div>
            <h2 style={{ fontSize: '28px', fontWeight: 'bold' }}>
                {typeof value === 'number' && title !== 'Orders' ? `$${value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : value}
            </h2>
        </div>
    );

    return (
        <div style={{ padding: '32px', maxWidth: '1400px', margin: '0 auto' }}>
            <header className="flex justify-between items-center mb-6">
                <div>
                    <h1 style={{ fontSize: '32px', fontWeight: 'bold' }}>Analytics Dashboard</h1>
                    <p style={{ color: 'var(--text-muted)' }}>Welcome back, {user?.email}</p>
                </div>
                <div className="flex gap-4 items-center">
                    <div className="flex items-center gap-2" style={{ backgroundColor: 'var(--bg-card)', padding: '8px 16px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                        <Calendar size={18} style={{ color: 'var(--text-muted)' }} />
                        <input
                            type="date"
                            value={date}
                            onChange={(e) => setDate(e.target.value)}
                            style={{ background: 'transparent', border: 'none', padding: 0 }}
                        />
                    </div>
                    <div className="flex items-center gap-2" style={{ backgroundColor: 'var(--bg-card)', padding: '8px 16px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                        <Filter size={18} style={{ color: 'var(--text-muted)' }} />
                        <select value={channel} onChange={(e) => setChannel(e.target.value)} style={{ background: 'transparent', border: 'none', padding: 0 }}>
                            <option value="">All Channels</option>
                            <option value="facebook">Facebook Ads</option>
                            <option value="Affiliate">Affiliates</option>
                            <option value="Organic">Organic</option>
                        </select>
                    </div>
                    <button onClick={() => navigate('/integrations')} className="flex items-center gap-2" style={{ backgroundColor: 'var(--bg-card)', color: 'var(--text-main)', border: '1px solid var(--border-color)' }}>
                        <Settings size={18} />
                        Integrations
                    </button>
                    <button onClick={logout} style={{ backgroundColor: 'var(--bg-hover)', color: 'var(--text-main)' }}>Logout</button>
                </div>
            </header>

            {/* KPI Section */}
            <div className="grid gap-6 mb-6" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))' }}>
                <KpiCard title="Total Revenue" value={kpi.totalRevenue} icon={DollarSign} colorClass="text-green-500" />
                <KpiCard title="Total Spend" value={kpi.totalSpend} icon={Activity} colorClass="text-red-500" />
                <KpiCard title="Net Profit" value={kpi.totalProfit} icon={ArrowUpRight} colorClass="text-primary" />
                <KpiCard title="Avg ROAS" value={kpi.avgROAS} icon={Target} colorClass="text-purple-500" />
            </div>

            {loading ? (
                <div className="flex justify-center items-center" style={{ height: '400px' }}>
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary" style={{ borderRightColor: 'transparent', borderRadius: '50%', border: '4px solid', animation: 'spin 1s linear infinite' }}></div>
                </div>
            ) : metrics.length === 0 ? (
                <div className="card text-center flex flex-col justify-center items-center" style={{ height: '400px' }}>
                    <Activity size={48} style={{ color: 'var(--text-muted)', marginBottom: '16px' }} />
                    <h3 style={{ fontSize: '20px', marginBottom: '8px' }}>No Data Available</h3>
                    <p style={{ color: 'var(--text-muted)' }}>Try selecting a different date or channel filter.</p>
                </div>
            ) : (
                <div className="grid gap-6" style={{ gridTemplateColumns: '1fr' }}>
                    {/* Main Chart */}
                    <div className="card">
                        <h3 className="mb-4" style={{ fontWeight: '600' }}>Revenue, Spend & Profit Trend</h3>
                        <div style={{ height: '400px' }}>
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={metrics} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)" />
                                    <XAxis dataKey="hour" stroke="var(--text-muted)" tickFormatter={(hour) => `${hour}:00`} />
                                    <YAxis stroke="var(--text-muted)" />
                                    <Tooltip contentStyle={{ backgroundColor: 'var(--bg-hover)', border: '1px solid var(--border-color)', borderRadius: '8px' }} />
                                    <Legend />
                                    <Line type="monotone" dataKey="revenue" stroke="var(--accent)" strokeWidth={3} dot={false} />
                                    <Line type="monotone" dataKey="adSpend" stroke="var(--danger)" strokeWidth={2} dot={false} />
                                    <Line type="monotone" dataKey="profit" stroke="var(--primary)" strokeWidth={3} dot={false} />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Secondary Charts */}
                    <div className="grid gap-6" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))' }}>
                        <div className="card">
                            <h3 className="mb-4" style={{ fontWeight: '600' }}>Hourly Orders</h3>
                            <div style={{ height: '300px' }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={metrics}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)" vertical={false} />
                                        <XAxis dataKey="hour" stroke="var(--text-muted)" />
                                        <YAxis stroke="var(--text-muted)" />
                                        <Tooltip cursor={{ fill: 'var(--bg-hover)' }} contentStyle={{ backgroundColor: 'var(--bg-hover)', border: 'none', borderRadius: '8px' }} />
                                        <Bar dataKey="orders" fill="var(--primary)" radius={[4, 4, 0, 0]} />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>

                        <div className="card">
                            <h3 className="mb-4" style={{ fontWeight: '600' }}>ROAS Trend</h3>
                            <div style={{ height: '300px' }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <LineChart data={metrics}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)" />
                                        <XAxis dataKey="hour" stroke="var(--text-muted)" />
                                        <YAxis stroke="var(--text-muted)" />
                                        <Tooltip contentStyle={{ backgroundColor: 'var(--bg-hover)', border: 'none', borderRadius: '8px' }} />
                                        <Line type="monotone" dataKey={(row) => row.adSpend > 0 ? (row.revenue / row.adSpend).toFixed(2) : 0} name="ROAS" stroke="#a855f7" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
