import { useState, useEffect } from 'react';
import { statisticsApi } from '../services/api.js';
import { TYPE_LABELS } from '../utils/constants.js';

function StatisticsView({ refreshKey }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [refreshKey]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [avgTime, slaRate, workerLoad, bottlenecks] = await Promise.all([
        statisticsApi.getAvgHandleTime(),
        statisticsApi.getSlaRate(),
        statisticsApi.getWorkerLoad(),
        statisticsApi.getBottlenecks(),
      ]);
      setData({ avgTime, slaRate, workerLoad, bottlenecks });
    } catch (error) {
      console.error('加载统计数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>加载中...</div>;
  if (!data) return <div>暂无数据</div>;

  return (
    <div>
      <h3 style={{ marginBottom: 20 }}>📊 统计分析</h3>

      <div className="stats-grid">
        <div className="stat-item">
          <h4>⏱️ 平均处理时长（不含挂起时间）</h4>
          {Object.entries(data.avgTime).map(([type, time]) => (
            <div key={type} className="detail-row">
              <span className="detail-label">{TYPE_LABELS[type] || type}</span>
              <span className="detail-value">{time}</span>
            </div>
          ))}
          {Object.keys(data.avgTime).length === 0 && (
            <div style={{ color: '#8c8c8c', fontSize: 13 }}>暂无数据</div>
          )}
        </div>

        <div className="stat-item">
          <h4>✅ SLA 达成率</h4>
          {Object.entries(data.slaRate).map(([type, rate]) => (
            <div key={type} className="detail-row">
              <span className="detail-label">{TYPE_LABELS[type] || type}</span>
              <span className="detail-value">{rate}</span>
            </div>
          ))}
          {Object.keys(data.slaRate).length === 0 && (
            <div style={{ color: '#8c8c8c', fontSize: 13 }}>暂无数据</div>
          )}
        </div>

        <div className="stat-item">
          <h4>🔥 瓶颈工位排行</h4>
          <ul className="bottleneck-list">
            {Object.entries(data.bottlenecks).map(([ws, count], idx) => (
              <li key={ws} className="bottleneck-item">
                <span>
                  <span style={{ color: '#ff4d4f', marginRight: 8 }}>#{idx + 1}</span>
                  {ws}
                </span>
                <span className="bottleneck-count">{count} 单</span>
              </li>
            ))}
            {Object.keys(data.bottlenecks).length === 0 && (
              <li style={{ color: '#8c8c8c', fontSize: 13 }}>暂无数据</li>
            )}
          </ul>
        </div>

        <div className="stat-item">
          <h4>👷 工人接单量</h4>
          {Object.entries(data.workerLoad).map(([workerId, count]) => (
            <div key={workerId} className="detail-row">
              <span className="detail-label">{workerId}</span>
              <span className="detail-value">{count} 单</span>
            </div>
          ))}
          {Object.keys(data.workerLoad).length === 0 && (
            <div style={{ color: '#8c8c8c', fontSize: 13 }}>暂无数据</div>
          )}
        </div>
      </div>
    </div>
  );
}

export default StatisticsView;
