package com.mercury.platform.shared.config.configration.impl;

import com.mercury.platform.core.misc.WhisperNotifierStatus;
import com.mercury.platform.shared.config.configration.BaseConfigurationService;
import com.mercury.platform.shared.config.configration.PlainConfigurationService;
import com.mercury.platform.shared.config.descriptor.ApplicationDescriptor;
import com.mercury.platform.shared.config.descriptor.ProfileDescriptor;

import java.util.HashMap;
import java.util.Map;


public class ApplicationConfigurationService extends BaseConfigurationService<ApplicationDescriptor> implements PlainConfigurationService<ApplicationDescriptor> {
    public ApplicationConfigurationService(ProfileDescriptor selectedProfile) {
        super(selectedProfile);
    }

    @Override
    public void validate() {
        if(this.selectedProfile.getApplicationDescriptor() == null) {
            this.selectedProfile.setApplicationDescriptor(this.getDefault());
        }
    }

    @Override
    public ApplicationDescriptor get() {
        return this.selectedProfile.getApplicationDescriptor();
    }

    @Override
    public void set(ApplicationDescriptor descriptor) {
        this.selectedProfile.setApplicationDescriptor(descriptor);
    }

    @Override
    public ApplicationDescriptor getDefault() {
        ApplicationDescriptor descriptor = new ApplicationDescriptor();
        descriptor.setNotifierStatus(WhisperNotifierStatus.ALWAYS);
        descriptor.setMinOpacity(100);
        descriptor.setMaxOpacity(100);
        descriptor.setGamePath("");
        descriptor.setShowOnStartUp(true);
        descriptor.setItemsGridEnable(true);
        descriptor.setCheckOutUpdate(true);
        descriptor.setInGameDnd(false);
        descriptor.setDndResponseText("Response text");
        System.out.println("qwe");
        return descriptor;
    }

    @Override
    public void toDefault() {
        this.selectedProfile.setApplicationDescriptor(this.getDefault());
    }
}
