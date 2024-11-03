package com.jindo.minipay.setting.event;

import com.jindo.minipay.setting.event.dto.CreateSettingEvent;
import com.jindo.minipay.setting.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class SettingEventListener {
    private final SettingService settingService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCreateSetting(CreateSettingEvent event) {
        settingService.createSetting((event.memberId()));
    }
}
