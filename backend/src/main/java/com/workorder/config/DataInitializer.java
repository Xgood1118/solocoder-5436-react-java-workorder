package com.workorder.config;

import com.workorder.model.AssignRule;
import com.workorder.model.WorkOrder;
import com.workorder.model.Worker;
import com.workorder.model.enums.Priority;
import com.workorder.model.enums.QualityProcessType;
import com.workorder.model.enums.WorkOrderStatus;
import com.workorder.model.enums.WorkOrderType;
import com.workorder.service.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {

    private final WorkerService workerService;
    private final WorkOrderService workOrderService;
    private final AssignService assignService;

    public DataInitializer(WorkerService workerService, WorkOrderService workOrderService,
                           AssignService assignService) {
        this.workerService = workerService;
        this.workOrderService = workOrderService;
        this.assignService = assignService;
    }

    @PostConstruct
    public void init() {
        initWorkers();
        initAssignRule();
        initWorkOrders();
    }

    private void initWorkers() {
        if (!workerService.getAll().isEmpty()) return;

        Worker w1 = new Worker();
        w1.setName("张三");
        w1.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan");
        w1.setWorkstation("WS-01");
        w1.setSkills(List.of("production", "quality"));
        workerService.create(w1);

        Worker w2 = new Worker();
        w2.setName("李四");
        w2.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=lisi");
        w2.setWorkstation("WS-01");
        w2.setSkills(List.of("production"));
        workerService.create(w2);

        Worker w3 = new Worker();
        w3.setName("王五");
        w3.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu");
        w3.setWorkstation("WS-02");
        w3.setSkills(List.of("maintenance", "mold"));
        workerService.create(w3);

        Worker w4 = new Worker();
        w4.setName("赵六");
        w4.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu");
        w4.setWorkstation("WS-02");
        w4.setSkills(List.of("quality", "material"));
        workerService.create(w4);

        Worker w5 = new Worker();
        w5.setName("钱七");
        w5.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=qianqi");
        w5.setWorkstation("WS-03");
        w5.setSkills(List.of("production", "maintenance"));
        workerService.create(w5);
    }

    private void initAssignRule() {
        if (!assignService.getAllRules().isEmpty()) return;

        AssignRule rule = new AssignRule();
        rule.setName("默认分派规则");
        rule.setEnabled(true);
        rule.setConsiderWorkstation(true);
        rule.setConsiderSkills(true);
        rule.setConsiderLoad(true);
        rule.setMaxLoadPerWorker(3);
        assignService.createRule(rule);
    }

    private void initWorkOrders() {
        if (!workOrderService.getAll().isEmpty()) return;

        WorkOrder wo1 = new WorkOrder();
        wo1.setTitle("生产工单-SO202401001");
        wo1.setType(WorkOrderType.PRODUCTION);
        wo1.setPriority(Priority.HIGH);
        wo1.setDescription("按销售订单SO202401001生产A型产品1000件");
        wo1.setCreator("车间主任");
        wo1.setWorkstation("WS-01");
        wo1.setPlannedQuantity(1000);
        wo1.setCompletedQuantity(0);
        wo1.setPlannedStartTime(LocalDateTime.now());
        wo1.setPlannedEndTime(LocalDateTime.now().plusDays(2));
        wo1.setOrderNumber("SO202401001");
        wo1.setProductionLine("PL-01");
        workOrderService.create(wo1);

        WorkOrder wo2 = new WorkOrder();
        wo2.setTitle("质量工单-BT202401001");
        wo2.setType(WorkOrderType.QUALITY);
        wo2.setPriority(Priority.URGENT);
        wo2.setDescription("QC巡检发现批次BT202401001有表面划伤不良");
        wo2.setCreator("QC巡检员");
        wo2.setWorkstation("WS-01");
        wo2.setBatchNumber("BT202401001");
        wo2.setDefectItem("划伤");
        wo2.setResponsibleProcess("喷涂工序");
        wo2.setSourceProductionOrderId(wo1.getId());
        workOrderService.create(wo2);

        WorkOrder wo3 = new WorkOrder();
        wo3.setTitle("设备工单-EQ202401001");
        wo3.setType(WorkOrderType.EQUIPMENT);
        wo3.setPriority(Priority.URGENT);
        wo3.setDescription("CNC-001设备主轴异响，需紧急维修");
        wo3.setCreator("设备操作员");
        wo3.setWorkstation("WS-02");
        wo3.setEquipmentCode("CNC-001");
        wo3.setFaultDescription("主轴运转时发出异常响声，转速不稳");
        workOrderService.create(wo3);

        WorkOrder wo4 = new WorkOrder();
        wo4.setTitle("物料工单-MT202401001");
        wo4.setType(WorkOrderType.MATERIAL);
        wo4.setPriority(Priority.MEDIUM);
        wo4.setDescription("WS-03产线缺A型原材料，需配送500kg");
        wo4.setCreator("产线班长");
        wo4.setWorkstation("WS-03");
        wo4.setMaterialCode("MAT-A-001");
        wo4.setRequiredQuantity(500);
        workOrderService.create(wo4);

        WorkOrder wo5 = new WorkOrder();
        wo5.setTitle("模具工单-MD202401001");
        wo5.setType(WorkOrderType.MOLD);
        wo5.setPriority(Priority.LOW);
        wo5.setDescription("MOLD-002模具到期保养");
        wo5.setCreator("模具管理员");
        wo5.setWorkstation("WS-02");
        wo5.setMoldCode("MOLD-002");
        wo5.setLastMaintenanceTime(LocalDateTime.now().minusMonths(3));
        workOrderService.create(wo5);

        WorkOrder wo6 = new WorkOrder();
        wo6.setTitle("生产工单-SO202401002");
        wo6.setType(WorkOrderType.PRODUCTION);
        wo6.setPriority(Priority.MEDIUM);
        wo6.setDescription("按销售订单SO202401002生产B型产品500件");
        wo6.setCreator("车间主任");
        wo6.setWorkstation("WS-03");
        wo6.setPlannedQuantity(500);
        wo6.setCompletedQuantity(0);
        wo6.setPlannedStartTime(LocalDateTime.now().plusHours(2));
        wo6.setPlannedEndTime(LocalDateTime.now().plusDays(3));
        wo6.setOrderNumber("SO202401002");
        wo6.setProductionLine("PL-03");
        workOrderService.create(wo6);

        WorkOrder wo7 = new WorkOrder();
        wo7.setTitle("质量工单-BT202401002");
        wo7.setType(WorkOrderType.QUALITY);
        wo7.setPriority(Priority.HIGH);
        wo7.setDescription("QC巡检发现批次BT202401002有色差不良");
        wo7.setCreator("QC巡检员");
        wo7.setWorkstation("WS-03");
        wo7.setBatchNumber("BT202401002");
        wo7.setDefectItem("色差");
        wo7.setResponsibleProcess("电镀工序");
        wo7.setSourceProductionOrderId(wo6.getId());
        workOrderService.create(wo7);

        WorkOrder wo8 = new WorkOrder();
        wo8.setTitle("设备工单-EQ202401002");
        wo8.setType(WorkOrderType.EQUIPMENT);
        wo8.setPriority(Priority.HIGH);
        wo8.setDescription("CNC-001设备历史故障-更换轴承");
        wo8.setCreator("设备维修员");
        wo8.setWorkstation("WS-02");
        wo8.setEquipmentCode("CNC-001");
        wo8.setFaultDescription("轴承磨损需更换");
        wo8.setStatus(WorkOrderStatus.COMPLETED);
        wo8.setCompletedAt(LocalDateTime.now().minusDays(15));
        workOrderService.create(wo8);
    }
}
