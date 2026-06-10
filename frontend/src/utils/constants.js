export const PRIORITY_LABELS = {
  URGENT: '紧急',
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低',
};

export const STATUS_LABELS = {
  CREATED: '已创建',
  ASSIGNED: '已分派',
  ACCEPTED: '已接单',
  IN_PROGRESS: '处理中',
  SUSPENDED: '已挂起',
  COMPLETED: '已完成',
  RETURNED: '已退回',
};

export const TYPE_LABELS = {
  PRODUCTION: '生产工单',
  QUALITY: '质量工单',
  EQUIPMENT: '设备工单',
  MATERIAL: '物料工单',
  MOLD: '模具工单',
};

export const QUALITY_PROCESS_LABELS = {
  REWORK: '返工',
  SCRAP: '报废',
};

export const getPriorityClass = (priority) => {
  const map = {
    URGENT: 'tag-urgent',
    HIGH: 'tag-high',
    MEDIUM: 'tag-medium',
    LOW: 'tag-low',
  };
  return map[priority] || '';
};

export const getStatusClass = (status) => `status-${status}`;

export const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const calculateProgress = (workOrder) => {
  if (workOrder.type === 'PRODUCTION' && workOrder.plannedQuantity) {
    return Math.round((workOrder.completedQuantity || 0) / workOrder.plannedQuantity * 100);
  }
  return 0;
};
