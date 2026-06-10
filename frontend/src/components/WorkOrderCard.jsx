import { PRIORITY_LABELS, STATUS_LABELS, TYPE_LABELS, getPriorityClass, getStatusClass, formatDate, calculateProgress } from '../utils/constants.js';

function WorkOrderCard({ workOrder, onClick, showProgress = false }) {
  const isUrgent = workOrder.priority === 'URGENT';
  const progress = calculateProgress(workOrder);

  return (
    <div
      className={`workorder-card ${isUrgent ? 'urgent' : ''}`}
      onClick={onClick}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <span className={`tag ${getPriorityClass(workOrder.priority)}`}>
          {PRIORITY_LABELS[workOrder.priority]}
        </span>
        <span className={`status-badge ${getStatusClass(workOrder.status)}`}>
          {STATUS_LABELS[workOrder.status]}
        </span>
      </div>

      <div className="wo-title" style={{ marginTop: 8 }}>{workOrder.title}</div>
      <div className="wo-type">{TYPE_LABELS[workOrder.type]}</div>

      {showProgress && workOrder.type === 'PRODUCTION' && (
        <div style={{ marginTop: 8 }}>
          <div style={{ fontSize: 12, color: '#666', marginBottom: 4 }}>
            进度: {workOrder.completedQuantity || 0} / {workOrder.plannedQuantity}
          </div>
          <div className="progress-bar">
            <div className="progress-bar-fill" style={{ width: `${progress}%` }}></div>
          </div>
        </div>
      )}

      <div className="wo-meta">
        <span>创建: {formatDate(workOrder.createdAt)}</span>
        {workOrder.assignee && (
          <span className="worker-avatar" title={workOrder.assignee}>
            {workOrder.assignee?.charAt(0)}
          </span>
        )}
      </div>
    </div>
  );
}

export default WorkOrderCard;
