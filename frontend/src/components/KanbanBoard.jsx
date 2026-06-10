import { useState, useEffect } from 'react';
import { queueApi, workerApi } from '../services/api.js';
import WorkOrderCard from './WorkOrderCard.jsx';
import { STATUS_LABELS } from '../utils/constants.js';

function KanbanBoard({ refreshKey, onViewOrder, onRefresh }) {
  const [workOrders, setWorkOrders] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, [refreshKey]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [orders, workerList] = await Promise.all([
        queueApi.getActive(),
        workerApi.getAll(),
      ]);
      setWorkOrders(orders);
      setWorkers(workerList);
    } catch (error) {
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const getWorkerName = (workerId) => {
    const worker = workers.find(w => w.id === workerId);
    return worker ? worker.name : workerId;
  };

  const groupByWorkstation = () => {
    const groups = {};
    workOrders.forEach(wo => {
      const ws = wo.workstation || '未分配';
      if (!groups[ws]) groups[ws] = [];
      groups[ws].push(wo);
    });
    return groups;
  };

  const groups = groupByWorkstation();

  if (loading) return <div>加载中...</div>;

  return (
    <div>
      <h3 style={{ marginBottom: 16 }}>📋 按工位分组看板</h3>
      <div className="kanban-board">
        {Object.entries(groups).map(([workstation, orders]) => (
          <div key={workstation} className="kanban-column">
            <div className="kanban-column-header">
              🔧 {workstation}
              <span className="count">{orders.length}</span>
            </div>
            {orders.map(wo => (
              <WorkOrderCard
                key={wo.id}
                workOrder={{
                  ...wo,
                  assignee: getWorkerName(wo.assignee),
                }}
                onClick={() => onViewOrder(wo.id)}
                showProgress={true}
              />
            ))}
            {orders.length === 0 && (
              <div style={{ color: '#bfbfbf', textAlign: 'center', padding: 20, fontSize: 13 }}>
                暂无工单
              </div>
            )}
          </div>
        ))}
        {Object.keys(groups).length === 0 && (
          <div style={{ color: '#bfbfbf', textAlign: 'center', padding: 40, gridColumn: '1/-1' }}>
            暂无活动工单
          </div>
        )}
      </div>
    </div>
  );
}

export default KanbanBoard;
