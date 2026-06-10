import { useState, useEffect } from 'react';
import { queueApi, workerApi, assignApi } from '../services/api.js';
import WorkOrderCard from './WorkOrderCard.jsx';
import { PRIORITY_LABELS, getPriorityClass } from '../utils/constants.js';

function QueueView({ refreshKey, onViewOrder, onRefresh }) {
  const [queue, setQueue] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [workerLoads, setWorkerLoads] = useState({});
  const [loading, setLoading] = useState(true);
  const [assigningOrderId, setAssigningOrderId] = useState(null);

  useEffect(() => {
    loadData();
  }, [refreshKey]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [queueData, workerList] = await Promise.all([
        queueApi.getQueue(),
        workerApi.getAll(),
      ]);
      setQueue(queueData);
      setWorkers(workerList);

      const loads = {};
      for (const w of workerList) {
        try {
          const loadData = await assignApi.getWorkerLoad(w.id);
          loads[w.id] = loadData.load;
        } catch (e) {
          loads[w.id] = 0;
        }
      }
      setWorkerLoads(loads);
    } catch (error) {
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleManualAssign = async (workOrderId, workerId) => {
    try {
      await assignApi.manualAssign(workOrderId, workerId, '当前用户');
      setAssigningOrderId(null);
      onRefresh();
    } catch (error) {
      alert('分派失败: ' + error.message);
    }
  };

  const handleAutoAssign = async (workOrderId) => {
    try {
      await assignApi.autoAssign(workOrderId, '当前用户');
      onRefresh();
    } catch (error) {
      alert('自动分派失败: ' + error.message);
    }
  };

  if (loading) return <div>加载中...</div>;

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>📥 待分派队列 ({queue.length})</h3>
      <div className="queue-list">
        {queue.map(wo => (
          <div key={wo.id} className="queue-item">
            <div className="queue-item-info" style={{ flex: 2 }}>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 4 }}>
                <span className={`tag ${getPriorityClass(wo.priority)}`}>
                  {PRIORITY_LABELS[wo.priority]}
                </span>
                <strong style={{ fontSize: 14 }}>{wo.title}</strong>
              </div>
              <div style={{ fontSize: 12, color: '#666' }}>{wo.description}</div>
              <div style={{ fontSize: 12, color: '#8c8c8c', marginTop: 4 }}>
                创建人: {wo.creator} | 工位: {wo.workstation || '未指定'}
              </div>
            </div>
            <div className="queue-item-actions">
              <button className="btn btn-default" onClick={() => onViewOrder(wo.id)}>
                详情
              </button>
              <button className="btn btn-primary" onClick={() => handleAutoAssign(wo.id)}>
                自动分派
              </button>
              <div style={{ position: 'relative' }}>
                <button
                  className="btn btn-success"
                  onClick={() => setAssigningOrderId(assigningOrderId === wo.id ? null : wo.id)}
                >
                  手动分派 ▼
                </button>
                {assigningOrderId === wo.id && (
                  <div style={{
                    position: 'absolute',
                    top: '100%',
                    right: 0,
                    background: 'white',
                    border: '1px solid #d9d9d9',
                    borderRadius: 4,
                    padding: 8,
                    zIndex: 100,
                    minWidth: 180,
                    boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                  }}>
                    <div style={{ fontSize: 12, color: '#8c8c8c', marginBottom: 6 }}>选择工人:</div>
                    {workers.map(w => {
                      const load = workerLoads[w.id] || 0;
                      const canAssign = load < 3;
                      return (
                        <div
                          key={w.id}
                          style={{
                            padding: '6px 8px',
                            cursor: canAssign ? 'pointer' : 'not-allowed',
                            borderRadius: 4,
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            opacity: canAssign ? 1 : 0.5,
                            fontSize: 13,
                          }}
                          onMouseEnter={(e) => {
                            if (canAssign) e.currentTarget.style.background = '#f0f5ff';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.background = 'transparent';
                          }}
                          onClick={() => canAssign && handleManualAssign(wo.id, w.id)}
                        >
                          <span>{w.name}</span>
                          <span style={{ fontSize: 11, color: '#8c8c8c' }}>
                            {load}/3
                          </span>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
        {queue.length === 0 && (
          <div style={{ textAlign: 'center', padding: 40, color: '#bfbfbf' }}>
            队列空空如也 🎉
          </div>
        )}
      </div>
    </div>
  );
}

export default QueueView;
