const API_BASE = '/api';

async function request(url, options = {}) {
  const response = await fetch(`${API_BASE}${url}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '请求失败' }));
    throw new Error(error.message || '请求失败');
  }

  if (response.status === 204) return null;
  return response.json();
}

export const workOrderApi = {
  getAll: (type) => request(`/workorders${type ? `?type=${type}` : ''}`),
  getById: (id) => request(`/workorders/${id}`),
  create: (data) => request('/workorders', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/workorders/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/workorders/${id}`, { method: 'DELETE' }),
  assign: (id, assignee, operator = 'system') =>
    request(`/workorders/${id}/assign`, { method: 'POST', body: JSON.stringify({ assignee, operator }) }),
  accept: (id, operator = 'system') =>
    request(`/workorders/${id}/accept`, { method: 'POST', body: JSON.stringify({ operator }) }),
  start: (id, operator = 'system') =>
    request(`/workorders/${id}/start`, { method: 'POST', body: JSON.stringify({ operator }) }),
  complete: (id, remark = '', operator = 'system') =>
    request(`/workorders/${id}/complete`, { method: 'POST', body: JSON.stringify({ remark, operator }) }),
  returnOrder: (id, reason, operator = 'system') =>
    request(`/workorders/${id}/return`, { method: 'POST', body: JSON.stringify({ reason, operator }) }),
  suspend: (id, reason, operator = 'system') =>
    request(`/workorders/${id}/suspend`, { method: 'POST', body: JSON.stringify({ reason, operator }) }),
  resume: (id, operator = 'system') =>
    request(`/workorders/${id}/resume`, { method: 'POST', body: JSON.stringify({ operator }) }),
  updateProgress: (id, completedQuantity, operator = 'system') =>
    request(`/workorders/${id}/progress`, { method: 'POST', body: JSON.stringify({ completedQuantity, operator }) }),
  getEquipmentHistory: (equipmentCode) => request(`/workorders/equipment/${equipmentCode}/history`),
  getQualityOrders: (productionOrderId) => request(`/workorders/production/${productionOrderId}/quality`),
};

export const queueApi = {
  getQueue: () => request('/queue'),
  getByStatus: (status) => request(`/queue/status/${status}`),
  getActive: () => request('/queue/active'),
};

export const assignApi = {
  manualAssign: (workOrderId, workerId, operator = 'system') =>
    request(`/assign/manual/${workOrderId}/${workerId}`, { method: 'POST', body: JSON.stringify({ operator }) }),
  autoAssign: (workOrderId, operator = 'system') =>
    request(`/assign/auto/${workOrderId}`, { method: 'POST', body: JSON.stringify({ operator }) }),
  canAssign: (workerId) => request(`/assign/can-assign/${workerId}`),
  getWorkerLoad: (workerId) => request(`/assign/worker-load/${workerId}`),
  getRules: () => request('/assign/rules'),
  getEnabledRule: () => request('/assign/rules/enabled'),
  createRule: (data) => request('/assign/rules', { method: 'POST', body: JSON.stringify(data) }),
  updateRule: (id, data) => request(`/assign/rules/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
};

export const workerApi = {
  getAll: (workstation) => request(`/workers${workstation ? `?workstation=${workstation}` : ''}`),
  getById: (id) => request(`/workers/${id}`),
  create: (data) => request('/workers', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/workers/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/workers/${id}`, { method: 'DELETE' }),
};

export const logApi = {
  getByWorkOrder: (workOrderId) => request(`/logs/workorder/${workOrderId}`),
  getAll: () => request('/logs'),
};

export const statisticsApi = {
  getAvgHandleTime: () => request('/statistics/avg-handle-time'),
  getSlaRate: () => request('/statistics/sla-rate'),
  getWorkerLoad: () => request('/statistics/worker-load'),
  getBottlenecks: () => request('/statistics/bottlenecks'),
  getDashboard: () => request('/statistics/dashboard'),
};
