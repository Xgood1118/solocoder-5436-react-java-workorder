import { useState, useEffect } from 'react';
import { statisticsApi } from '../services/api.js';

function Dashboard({ refreshKey, onViewOrder }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, [refreshKey]);

  const loadStats = async () => {
    try {
      setLoading(true);
      const data = await statisticsApi.getDashboard();
      setStats(data);
    } catch (error) {
      console.error('加载统计数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>加载中...</div>;
  if (!stats) return <div>暂无数据</div>;

  const statItems = [
    { label: '总工单', value: stats.total, color: '#1890ff' },
    { label: '待分派', value: stats.created, color: '#1890ff' },
    { label: '处理中', value: stats.inProgress, color: '#13c2c2' },
    { label: '已挂起', value: stats.suspended, color: '#8c8c8c' },
    { label: '已退回', value: stats.returned, color: '#ff4d4f' },
    { label: '已完成', value: stats.completed, color: '#52c41a' },
  ];

  return (
    <div>
      <div className="dashboard-stats">
        {statItems.map(item => (
          <div key={item.label} className="stat-card">
            <div className="stat-label">{item.label}</div>
            <div className="stat-value" style={{ color: item.color }}>{item.value}</div>
          </div>
        ))}
      </div>

      <div className="stats-grid">
        <div className="stat-item">
          <h4>📊 平均处理时长</h4>
          {stats.avgHandleTime && Object.entries(stats.avgHandleTime).map(([type, time]) => (
            <div key={type} className="detail-row">
              <span className="detail-label">{type}</span>
              <span className="detail-value">{time}</span>
            </div>
          ))}
        </div>

        <div className="stat-item">
          <h4>✅ SLA 达成率</h4>
          {stats.slaRate && Object.entries(stats.slaRate).map(([type, rate]) => (
            <div key={type} className="detail-row">
              <span className="detail-label">{type}</span>
              <span className="detail-value">{rate}</span>
            </div>
          ))}
        </div>

        <div className="stat-item">
          <h4>🔥 瓶颈工位</h4>
          <ul className="bottleneck-list">
            {stats.bottlenecks && Object.entries(stats.bottlenecks).map(([ws, count]) => (
              <li key={ws} className="bottleneck-item">
                <span>{ws}</span>
                <span className="bottleneck-count">{count} 单</span>
              </li>
            ))}
            {(!stats.bottlenecks || Object.keys(stats.bottlenecks).length === 0) && (
              <li style={{ color: '#8c8c8c', fontSize: 13 }}>暂无数据</li>
            )}
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
