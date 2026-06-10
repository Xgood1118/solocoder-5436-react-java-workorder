import { useState, useEffect } from 'react';
import { workerApi, statisticsApi } from '../services/api.js';

function WorkersView({ refreshKey }) {
  const [workers, setWorkers] = useState([]);
  const [workerLoads, setWorkerLoads] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [refreshKey]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [workerList, loads] = await Promise.all([
        workerApi.getAll(),
        statisticsApi.getWorkerLoad(),
      ]);
      setWorkers(workerList);
      setWorkerLoads(loads);
    } catch (error) {
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const getLoadClass = (load) => {
    if (load >= 3) return 'danger';
    if (load >= 2) return 'warning';
    return '';
  };

  if (loading) return <div>加载中...</div>;

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>👷 工人管理 ({workers.length}人)</h3>
      <div className="worker-list">
        {workers.map(worker => {
          const load = workerLoads[worker.id] || 0;
          const loadPercent = (load / 3) * 100;
          return (
            <div key={worker.id} className="worker-card">
              <div className="avatar">
                {worker.avatar ? (
                  <img src={worker.avatar} alt={worker.name} />
                ) : (
                  worker.name.charAt(0)
                )}
              </div>
              <div className="name">{worker.name}</div>
              <div className="ws">📍 {worker.workstation}</div>
              <div className="load-bar">
                <div
                  className={`load-fill ${getLoadClass(load)}`}
                  style={{ width: `${loadPercent}%` }}
                ></div>
              </div>
              <div className="load-text">当前负载: {load}/3 单</div>
              {worker.skills && worker.skills.length > 0 && (
                <div style={{ marginTop: 8, fontSize: 11, color: '#8c8c8c' }}>
                  技能: {worker.skills.join(', ')}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default WorkersView;
