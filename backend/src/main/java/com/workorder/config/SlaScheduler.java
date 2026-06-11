package com.workorder.config;

import com.workorder.service.WorkOrderService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SlaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SlaScheduler.class);

    private final WorkOrderService workOrderService;

    public SlaScheduler(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostConstruct
    public void init() {
        logger.info("SLA 定时任务调度器已初始化，每分钟检查一次退回工单 SLA 过期情况");
    }

    @Scheduled(fixedRate = 60000)
    public void checkReturnedOrderSla() {
        try {
            int escalated = workOrderService.escalateExpiredReturnedOrders();
            if (escalated > 0) {
                logger.info("SLA 退回工单检查完成，自动升级了 {} 张工单", escalated);
            } else {
                logger.debug("SLA 退回工单检查完成，无过期工单");
            }
        } catch (Exception e) {
            logger.error("SLA 检查任务执行失败", e);
        }
    }
}
