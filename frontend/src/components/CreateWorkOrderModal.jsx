import { useState } from 'react';
import { workOrderApi } from '../services/api.js';
import { TYPE_LABELS, PRIORITY_LABELS } from '../utils/constants.js';

function CreateWorkOrderModal({ onClose, onSuccess }) {
  const [type, setType] = useState('PRODUCTION');
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    creator: '当前用户',
    workstation: '',
    plannedQuantity: '',
    orderNumber: '',
    productionLine: '',
    operator: '',
    batchNumber: '',
    defectItem: '',
    responsibleProcess: '',
    equipmentCode: '',
    faultDescription: '',
    materialCode: '',
    requiredQuantity: '',
    moldCode: '',
    sourceProductionOrderId: '',
  });

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      alert('请输入工单标题');
      return;
    }

    try {
      const data = {
        type,
        title: formData.title,
        description: formData.description,
        priority: formData.priority,
        creator: formData.creator,
        workstation: formData.workstation || undefined,
      };

      if (type === 'PRODUCTION') {
        data.plannedQuantity = parseInt(formData.plannedQuantity) || 0;
        data.orderNumber = formData.orderNumber || undefined;
        data.productionLine = formData.productionLine || undefined;
        data.operator = formData.operator || undefined;
      } else if (type === 'QUALITY') {
        data.batchNumber = formData.batchNumber || undefined;
        data.defectItem = formData.defectItem || undefined;
        data.responsibleProcess = formData.responsibleProcess || undefined;
        data.sourceProductionOrderId = formData.sourceProductionOrderId || undefined;
      } else if (type === 'EQUIPMENT') {
        data.equipmentCode = formData.equipmentCode || undefined;
        data.faultDescription = formData.faultDescription || undefined;
      } else if (type === 'MATERIAL') {
        data.materialCode = formData.materialCode || undefined;
        data.requiredQuantity = parseInt(formData.requiredQuantity) || 0;
      } else if (type === 'MOLD') {
        data.moldCode = formData.moldCode || undefined;
      }

      await workOrderApi.create(data);
      onSuccess();
    } catch (error) {
      alert('创建失败: ' + error.message);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>新建工单</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-row">
              <div className="form-item">
                <label>工单类型 *</label>
                <select value={type} onChange={e => setType(e.target.value)}>
                  {Object.entries(TYPE_LABELS).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
              <div className="form-item">
                <label>优先级</label>
                <select value={formData.priority} onChange={e => handleChange('priority', e.target.value)}>
                  {Object.entries(PRIORITY_LABELS).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-item">
              <label>工单标题 *</label>
              <input
                type="text"
                value={formData.title}
                onChange={e => handleChange('title', e.target.value)}
                placeholder="请输入工单标题"
              />
            </div>

            <div className="form-item">
              <label>问题描述</label>
              <textarea
                value={formData.description}
                onChange={e => handleChange('description', e.target.value)}
                placeholder="请输入详细描述"
              />
            </div>

            <div className="form-row">
              <div className="form-item">
                <label>所属工位</label>
                <input
                  type="text"
                  value={formData.workstation}
                  onChange={e => handleChange('workstation', e.target.value)}
                  placeholder="如 WS-01"
                />
              </div>
              <div className="form-item">
                <label>创建人</label>
                <input
                  type="text"
                  value={formData.creator}
                  onChange={e => handleChange('creator', e.target.value)}
                />
              </div>
            </div>

            {type === 'PRODUCTION' && (
              <>
                <h4 style={{ margin: '12px 0', fontSize: 14, color: '#262626' }}>生产信息</h4>
                <div className="form-row">
                  <div className="form-item">
                    <label>销售订单号</label>
                    <input
                      type="text"
                      value={formData.orderNumber}
                      onChange={e => handleChange('orderNumber', e.target.value)}
                      placeholder="如 SO202401001"
                    />
                  </div>
                  <div className="form-item">
                    <label>计划数量</label>
                    <input
                      type="number"
                      value={formData.plannedQuantity}
                      onChange={e => handleChange('plannedQuantity', e.target.value)}
                      placeholder="请输入计划数量"
                    />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-item">
                    <label>产线编号</label>
                    <input
                      type="text"
                      value={formData.productionLine}
                      onChange={e => handleChange('productionLine', e.target.value)}
                      placeholder="如 PL-01"
                    />
                  </div>
                  <div className="form-item">
                    <label>操作工人</label>
                    <input
                      type="text"
                      value={formData.operator}
                      onChange={e => handleChange('operator', e.target.value)}
                    />
                  </div>
                </div>
              </>
            )}

            {type === 'QUALITY' && (
              <>
                <h4 style={{ margin: '12px 0', fontSize: 14, color: '#262626' }}>质量信息</h4>
                <div className="form-row">
                  <div className="form-item">
                    <label>批次号</label>
                    <input
                      type="text"
                      value={formData.batchNumber}
                      onChange={e => handleChange('batchNumber', e.target.value)}
                      placeholder="如 BT202401001"
                    />
                  </div>
                  <div className="form-item">
                    <label>不良项目</label>
                    <input
                      type="text"
                      value={formData.defectItem}
                      onChange={e => handleChange('defectItem', e.target.value)}
                      placeholder="如 划伤、色差、尺寸偏差"
                    />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-item">
                    <label>责任工序</label>
                    <input
                      type="text"
                      value={formData.responsibleProcess}
                      onChange={e => handleChange('responsibleProcess', e.target.value)}
                      placeholder="如 喷涂工序"
                    />
                  </div>
                  <div className="form-item">
                    <label>来源生产工单ID</label>
                    <input
                      type="text"
                      value={formData.sourceProductionOrderId}
                      onChange={e => handleChange('sourceProductionOrderId', e.target.value)}
                      placeholder="可选，关联生产工单"
                    />
                  </div>
                </div>
              </>
            )}

            {type === 'EQUIPMENT' && (
              <>
                <h4 style={{ margin: '12px 0', fontSize: 14, color: '#262626' }}>设备信息</h4>
                <div className="form-row">
                  <div className="form-item">
                    <label>设备编号</label>
                    <input
                      type="text"
                      value={formData.equipmentCode}
                      onChange={e => handleChange('equipmentCode', e.target.value)}
                      placeholder="如 CNC-001"
                    />
                  </div>
                  <div className="form-item">
                    <label>故障现象</label>
                    <input
                      type="text"
                      value={formData.faultDescription}
                      onChange={e => handleChange('faultDescription', e.target.value)}
                      placeholder="简要描述故障"
                    />
                  </div>
                </div>
              </>
            )}

            {type === 'MATERIAL' && (
              <>
                <h4 style={{ margin: '12px 0', fontSize: 14, color: '#262626' }}>物料信息</h4>
                <div className="form-row">
                  <div className="form-item">
                    <label>物料编号</label>
                    <input
                      type="text"
                      value={formData.materialCode}
                      onChange={e => handleChange('materialCode', e.target.value)}
                      placeholder="如 MAT-A-001"
                    />
                  </div>
                  <div className="form-item">
                    <label>需求数量</label>
                    <input
                      type="number"
                      value={formData.requiredQuantity}
                      onChange={e => handleChange('requiredQuantity', e.target.value)}
                      placeholder="请输入需求数量"
                    />
                  </div>
                </div>
              </>
            )}

            {type === 'MOLD' && (
              <>
                <h4 style={{ margin: '12px 0', fontSize: 14, color: '#262626' }}>模具信息</h4>
                <div className="form-item">
                  <label>模具编号</label>
                  <input
                    type="text"
                    value={formData.moldCode}
                    onChange={e => handleChange('moldCode', e.target.value)}
                    placeholder="如 MOLD-002"
                  />
                </div>
              </>
            )}
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-default" onClick={onClose}>取消</button>
            <button type="submit" className="btn btn-primary">创建</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreateWorkOrderModal;
