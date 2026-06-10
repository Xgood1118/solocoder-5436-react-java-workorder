import { useState, useEffect } from 'react';
import Dashboard from './components/Dashboard.jsx';
import KanbanBoard from './components/KanbanBoard.jsx';
import QueueView from './components/QueueView.jsx';
import WorkersView from './components/WorkersView.jsx';
import StatisticsView from './components/StatisticsView.jsx';
import WorkOrderModal from './components/WorkOrderModal.jsx';
import CreateWorkOrderModal from './components/CreateWorkOrderModal.jsx';

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const refresh = () => setRefreshKey(prev => prev + 1);

  const tabs = [
    { key: 'dashboard', label: '仪表盘' },
    { key: 'kanban', label: '工单看板' },
    { key: 'queue', label: '待分派队列' },
    { key: 'workers', label: '工人管理' },
    { key: 'statistics', label: '统计分析' },
  ];

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return <Dashboard refreshKey={refreshKey} onViewOrder={setSelectedOrder} />;
      case 'kanban':
        return <KanbanBoard refreshKey={refreshKey} onViewOrder={setSelectedOrder} onRefresh={refresh} />;
      case 'queue':
        return <QueueView refreshKey={refreshKey} onViewOrder={setSelectedOrder} onRefresh={refresh} />;
      case 'workers':
        return <WorkersView refreshKey={refreshKey} />;
      case 'statistics':
        return <StatisticsView refreshKey={refreshKey} />;
      default:
        return null;
    }
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>🏭 车间工单管理系统</h1>
        <nav className="app-nav">
          {tabs.map(tab => (
            <button
              key={tab.key}
              className={`nav-tab ${activeTab === tab.key ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.label}
            </button>
          ))}
          <button
            className="nav-tab"
            style={{ marginLeft: 'auto', background: '#fff', color: '#1890ff', fontWeight: 500 }}
            onClick={() => setShowCreateModal(true)}
          >
            + 新建工单
          </button>
        </nav>
      </header>

      <main className="app-content">
        {renderContent()}
      </main>

      {selectedOrder && (
        <WorkOrderModal
          workOrderId={selectedOrder}
          onClose={() => setSelectedOrder(null)}
          onRefresh={refresh}
        />
      )}

      {showCreateModal && (
        <CreateWorkOrderModal
          onClose={() => setShowCreateModal(false)}
          onSuccess={() => {
            setShowCreateModal(false);
            refresh();
          }}
        />
      )}
    </div>
  );
}

export default App;
