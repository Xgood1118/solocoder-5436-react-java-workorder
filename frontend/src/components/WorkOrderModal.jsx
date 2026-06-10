import { useState, useEffect } from 'react';
import { workOrderApi, logApi, workerApi } from '../services/api.js';
import {
  PRIORITY_LABELS, STATUS_LABELS, TYPE_LABELS, QUALITY_PROCESS_LABELS,
  getPriorityClass, getStatusClass, formatDate, calculateProgress
} from '../utils/constants.js';

function WorkOrderModal({ workOrderId, onClose, onRefresh }) {
  const [workOrder, setWorkOrder] = useState(null);
  const [logs, setLogs] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('info');
  const [actionInput, setActionInput] = useState('');
  const [showActionInput, setShowActionInput] = useState(null);
  const [equipmentHistory, setEquipmentHistory] = useState(null);
  const [qualityOrders, setQualityOrders] = useState(null);

  useEffect(() => {
    loadData();
  }, [workOrderId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [wo, logList, workerList] = await Promise.all([
        workOrderApi.getById(workOrderId),
        logApi.getByWorkOrder(workOrderId),
        workerApi.getAll(),
      ]);
      setWorkOrder(wo);
      setLogs(logList);
      setWorkers(workerList);
    } catch (error) {
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const getWorkerName = (workerId) => {
    if (!workerId) return '-';
    const worker = workers.find(w => w.id === workerId);
    return worker ? worker.name : workerId;
  };

  const handleAction = async (action) => {
    try {
      let result;
      switch (action) {
        case 'accept':
          result = await workOrderApi.accept(workOrderId, '当前用户');
          break;
        case 'start':
          result = await workOrderApi.start(workOrderId, '当前用户');
          break;
        case 'complete':
          result = await workOrderApi.complete(workOrderId, actionInput, '当前用户');
          break;
        case 'return':
          result = await workOrderApi.returnOrder(workOrderId, actionInput, '当前用户');
          break;
        case 'suspend':
          result = await workOrderApi.suspend(workOrderId, actionInput, '当前用户');
          break;
        case 'resume':
          result = await workOrderApi.resume(workOrderId, '当前用户');
          break;
        default:
          return;
      }
      setWorkOrder(result);
      setShowActionInput(null);
      setActionInput('');
      loadData();
      onRefresh();
    } catch (error) {
      alert('操作失败: ' + error.message);
    }
  };

  const handleUpdateProgress = async () => {
    const qty = parseInt(actionInput);
    if (isNaN(qty) || qty < 0) {
      alert('请输入有效的数量');
      return;
    }
    try {
      const result = await workOrderApi.updateProgress(workOrderId, qty, '当前用户');
      setWorkOrder(result);
      setShowActionInput(null);
      setActionInput('');
      loadData();
      onRefresh();
    } catch (error) {
      alert('更新失败: ' + error.message);
    }
  };

  const loadEquipmentHistory = async () => {
    try {
      const history = await workOrderApi.getEquipmentHistory(workOrder.equipmentCode);
      setEquipmentHistory(history);
    } catch (error) {
      alert('加载历史记录失败: ' + error.message);
    }
  };

  const loadQualityOrders = async () => {
    try {
      const orders = await workOrderApi.getQualityOrders(workOrderId);
      setQualityOrders(orders);
    } catch (error) {
      alert('加载质量工单失败: ' + error.message);
    }
  };

  const canAccept = workOrder?.status === 'ASSIGNED';
  const canStart = workOrder?.status === 'ACCEPTED' || workOrder?.status === 'SUSPENDED';
  const canComplete = workOrder?.status === 'IN_PROGRESS';
  const canReturn = workOrder?.status === 'IN_PROGRESS' || workOrder?.status === 'ACCEPTED';
  const canSuspend = workOrder?.status === 'IN_PROGRESS';
  const canResume = workOrder?.status === 'SUSPENDED';

  if (loading) return <div className="modal-overlay"><div className="modal">加载中...</div></div>;
  if (!workOrder) return null;

  const progress = calculateProgress(workOrder);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>
            <span className={`tag ${getPriorityClass(workOrder.priority)}`} style={{ marginRight: 8 }}>
              {PRIORITY_LABELS[workOrder.priority]}
            </span>
            {workOrder.title}
          </h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div style={{ padding: '0 24px', marginTop: 12 }}>
          <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
            <span className={`status-badge ${getStatusClass(workOrder.status)}`}>
              {STATUS_LABELS[workOrder.status]}
            </span>
            <span style={{ fontSize: 13, color: '#8c8c8c' }}>
              {TYPE_LABELS[workOrder.type]}
            </span>
          </div>
          {workOrder.type === 'PRODUCTION' && (
            <div style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 12, color: '#666', marginBottom: 4 }}>
                进度: {workOrder.completedQuantity || 0} / {workOrder.plannedQuantity} ({progress}%)
              </div>
              <div className="progress-bar">
                <div className="progress-bar-fill" style={{ width: `${progress}%` }}></div>
              </div>
            </div>
          )}
        </div>

        <div className="tabs" style={{ margin: '0 24px' }}>
          <button
            className={`tab ${activeTab === 'info' ? 'active' : ''}`}
            onClick={() => setActiveTab('info')}
          >
            基本信息
          </button>
          <button
            className={`tab ${activeTab === 'logs' ? 'active' : ''}`}
            onClick={() => setActiveTab('logs')}
          >
            流转日志
          </button>
          {workOrder.type === 'EQUIPMENT' && (
            <button
              className={`tab ${activeTab === 'history' ? 'active' : ''}`}
              onClick={() => { setActiveTab('history'); if (!equipmentHistory) loadEquipmentHistory(); }}
            >
              设备历史
            </button>
          )}
          {workOrder.type === 'PRODUCTION' && (
            <button
              className={`tab ${activeTab === 'quality' ? 'active' : ''}`}
              onClick={() => { setActiveTab('quality'); if (!qualityOrders) loadQualityOrders(); }}
            >
              关联质量单
            </button>
          )}
        </div>

        <div className="modal-body">
          {activeTab === 'info' && (
            <div>
              <div className="detail-row">
                <span className="detail-label">工单ID</span>
                <span className="detail-value">{workOrder.id}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">工单描述</span>
                <span className="detail-value">{workOrder.description || '-'}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">创建人</span>
                <span className="detail-value">{workOrder.creator || '-'}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">当前处理人</span>
                <span className="detail-value">{getWorkerName(workOrder.assignee)}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">所属工位</span>
                <span className="detail-value">{workOrder.workstation || '-'}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">创建时间</span>
                <span className="detail-value">{formatDate(workOrder.createdAt)}</span>
              </div>

              {workOrder.type === 'PRODUCTION' && (
                <>
                  <h4 style={{ margin: '16px 0 8px', fontSize: 14, color: '#262626' }}>生产信息</h4>
                  <div className="detail-row">
                    <span className="detail-label">销售订单号</span>
                    <span className="detail-value">{workOrder.orderNumber || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">产线编号</span>
                    <span className="detail-value">{workOrder.productionLine || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">操作工人</span>
                    <span className="detail-value">{workOrder.operator || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">计划数量</span>
                    <span className="detail-value">{workOrder.plannedQuantity || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">已完成数量</span>
                    <span className="detail-value">{workOrder.completedQuantity || 0}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">计划开始</span>
                    <span className="detail-value">{formatDate(workOrder.plannedStartTime)}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">计划结束</span>
                    <span className="detail-value">{formatDate(workOrder.plannedEndTime)}</span>
                  </div>
                </>
              )}

              {workOrder.type === 'QUALITY' && (
                <>
                  <h4 style={{ margin: '16px 0 8px', fontSize: 14, color: '#262626' }}>质量信息</h4>
                  <div className="detail-row">
                    <span className="detail-label">批次号</span>
                    <span className="detail-value">{workOrder.batchNumber || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">不良项目</span>
                    <span className="detail-value">{workOrder.defectItem || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">责任工序</span>
                    <span className="detail-value">{workOrder.responsibleProcess || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">处理方式</span>
                    <span className="detail-value">
                      {workOrder.qualityProcessType ? QUALITY_PROCESS_LABELS[workOrder.qualityProcessType] : '-'}
                    </span>
                  </div>
                  {workOrder.sourceProductionOrderId && (
                    <div className="detail-row">
                      <span className="detail-label">来源生产单</span>
                      <span className="detail-value link-text">
                        {workOrder.sourceProductionOrderId}
                      </span>
                    </div>
                  )}
                </>
              )}

              {workOrder.type === 'EQUIPMENT' && (
                <>
                  <h4 style={{ margin: '16px 0 8px', fontSize: 14, color: '#262626' }}>设备信息</h4>
                  <div className="detail-row">
                    <span className="detail-label">设备编号</span>
                    <span className="detail-value">{workOrder.equipmentCode || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">故障描述</span>
                    <span className="detail-value">{workOrder.faultDescription || '-'}</span>
                  </div>
                </>
              )}

              {workOrder.type === 'MATERIAL' && (
                <>
                  <h4 style={{ margin: '16px 0 8px', fontSize: 14, color: '#262626' }}>物料信息</h4>
                  <div className="detail-row">
                    <span className="detail-label">物料编号</span>
                    <span className="detail-value">{workOrder.materialCode || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">需求数量</span>
                    <span className="detail-value">{workOrder.requiredQuantity || '-'}</span>
                  </div>
                </>
              )}

              {workOrder.type === 'MOLD' && (
                <>
                  <h4 style={{ margin: '16px 0 8px', fontSize: 14, color: '#262626' }}>模具信息</h4>
                  <div className="detail-row">
                    <span className="detail-label">模具编号</span>
                    <span className="detail-value">{workOrder.moldCode || '-'}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">上次保养</span>
                    <span className="detail-value">{formatDate(workOrder.lastMaintenanceTime)}</span>
                  </div>
                </>
              )}

              {workOrder.totalSuspendedMinutes > 0 && (
                <div className="detail-row" style={{ marginTop: 12, color: '#faad14' }}>
                  <span className="detail-label">累计挂起</span>
                  <span className="detail-value">{workOrder.totalSuspendedMinutes} 分钟</span>
                </div>
              )}

              {workOrder.returnReason && (
                <div className="detail-row" style={{ color: '#ff4d4f' }}>
                  <span className="detail-label">退回原因</span>
                  <span className="detail-value">{workOrder.returnReason}</span>
                </div>
              )}
            </div>
          )}

          {activeTab === 'logs' && (
            <ul className="log-list">
              {logs.map(log => (
                <li key={log.id} className="log-item">
                  <span className="log-time">{formatDate(log.operationTime)}</span>
                  <div className="log-content">
                    <div>
                      <span className="log-operation">{log.operation}</span>
                      <span className="log-operator">{log.operator}</span>
                      {log.fromStatus && log.toStatus && (
                        <span style={{ color: '#8c8c8c', fontSize: 12 }}>
                          ({STATUS_LABELS[log.fromStatus]} → {STATUS_LABELS[log.toStatus]})
                        </span>
                      )}
                    </div>
                    {log.remark && <div className="log-remark">{log.remark}</div>}
                  </div>
                </li>
              ))}
              {logs.length === 0 && (
                <li style={{ textAlign: 'center', padding: 20, color: '#bfbfbf' }}>暂无日志</li>
              )}
            </ul>
          )}

          {activeTab === 'history' && equipmentHistory && (
            <div>
              <div style={{ marginBottom: 12, fontSize: 13, color: '#666' }}>
                设备 {workOrder.equipmentCode} 历史报修记录 ({equipmentHistory.length}次)
              </div>
              {equipmentHistory.map(wo => (
                <div key={wo.id} className="detail-row" style={{ cursor: 'pointer' }}>
                  <span className="detail-label">{formatDate(wo.createdAt)}</span>
                  <span className="detail-value">
                    <span className={`status-badge ${getStatusClass(wo.status)}`} style={{ marginRight: 8 }}>
                      {STATUS_LABELS[wo.status]}
                    </span>
                    {wo.faultDescription || wo.title}
                  </span>
                </div>
              ))}
              {equipmentHistory.length === 0 && (
                <div style={{ textAlign: 'center', padding: 20, color: '#bfbfbf' }}>暂无历史记录</div>
              )}
            </div>
          )}

          {activeTab === 'quality' && qualityOrders && (
            <div>
              <div style={{ marginBottom: 12, fontSize: 13, color: '#666' }}>
                本生产单触发的质量工单 ({qualityOrders.length}张)
              </div>
              {qualityOrders.map(wo => (
                <div key={wo.id} className="detail-row">
                  <span className="detail-label">{PRIORITY_LABELS[wo.priority]}</span>
                  <span className="detail-value">
                    <span className={`status-badge ${getStatusClass(wo.status)}`} style={{ marginRight: 8 }}>
                      {STATUS_LABELS[wo.status]}
                    </span>
                    {wo.defectItem} - {wo.title}
                  </span>
                </div>
              ))}
              {qualityOrders.length === 0 && (
                <div style={{ textAlign: 'center', padding: 20, color: '#bfbfbf' }}>暂无质量工单</div>
              )}
            </div>
          )}
        </div>

        <div className="modal-footer">
          {showActionInput && (
            <div style={{ flex: 1, marginRight: 12 }}>
              <input
                type="text"
                placeholder={
                  showActionInput === 'return' ? '请输入退回原因...' :
                  showActionInput === 'suspend' ? '请输入挂起原因...' :
                  showActionInput === 'complete' ? '请输入完成备注...' :
                  showActionInput === 'progress' ? '请输入已完成数量...' : ''
                }
                value={actionInput}
                onChange={e => setActionInput(e.target.value)}
                style={{ width: '100%', padding: '6px 10px', border: '1px solid #d9d9d9', borderRadius: 4 }}
              />
            </div>
          )}

          <button className="btn btn-default" onClick={onClose}>关闭</button>

          {canAccept && (
            <button className="btn btn-primary" onClick={() => handleAction('accept')}>接单</button>
          )}
          {canStart && (
            <button className="btn btn-primary" onClick={() => handleAction('start')}>开始处理</button>
          )}
          {canComplete && (
            showActionInput === 'complete' ? (
              <button className="btn btn-success" onClick={() => handleAction('complete')}>确认完成</button>
            ) : (
              <button className="btn btn-success" onClick={() => setShowActionInput('complete')}>完成</button>
            )
          )}
          {canReturn && (
            showActionInput === 'return' ? (
              <button className="btn btn-danger" onClick={() => handleAction('return')}>确认退回</button>
            ) : (
              <button className="btn btn-danger" onClick={() => setShowActionInput('return')}>退回</button>
            )
          )}
          {canSuspend && (
            showActionInput === 'suspend' ? (
              <button className="btn btn-warning" onClick={() => handleAction('suspend')}>确认挂起</button>
            ) : (
              <button className="btn btn-warning" onClick={() => setShowActionInput('suspend')}>挂起</button>
            )
          )}
          {canResume && (
            <button className="btn btn-primary" onClick={() => handleAction('resume')}>恢复</button>
          )}
          {workOrder.type === 'PRODUCTION' && canComplete && (
            showActionInput === 'progress' ? (
              <button className="btn btn-primary" onClick={handleUpdateProgress}>更新进度</button>
            ) : (
              <button className="btn btn-default" onClick={() => setShowActionInput('progress')}>更新进度</button>
            )
          )}
        </div>
      </div>
    </div>
  );
}

export default WorkOrderModal;
