package com.wso2.carbon.custom.attribute.store.internal;


import com.wso2.carbon.custom.attribute.store.CustomAttributeStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "com.wso2.carbon.custom.attribute.store.component",
        immediate = true
)
public class CustomAttributeStoreComponent {

    private static Log log = LogFactory.getLog(CustomAttributeStoreComponent.class);
    private static RealmService realmService;

    @Activate
    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(UserOperationEventListener.class.getName(), new CustomAttributeStore(), null);
        log.info("CustomAttributeStore bundle activated successfully..");
    }

    @Deactivate
    protected void deActivate(BundleContext context) {
        if (log.isDebugEnabled()) {
            log.info("CustomAttributeStore bundle is deactivated");
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service");
        CustomAttributeStoreComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        CustomAttributeStoreComponent.realmService = null;
    }

    public static RealmService getRealmService(){
        return CustomAttributeStoreComponent.realmService;
    }
}
