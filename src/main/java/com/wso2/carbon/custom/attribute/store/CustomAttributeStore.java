package com.wso2.carbon.custom.attribute.store;


import com.wso2.carbon.custom.attribute.store.internal.CustomAttributeStoreComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.common.UserUniqueIDManger;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Map;

/**
 *
 */
public class CustomAttributeStore extends AbstractIdentityUserOperationEventListener {

    private static final String ATTRIBUTE_STORE_SUFFIX = "-ATTRIBUTE-STORE";
    private static Log log = LogFactory.getLog(CustomAttributeStore.class);

    @Override
    public int getExecutionOrderId() {
        return 9003;
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        log.info("Custom Attribute Store , doPreAuthenticate method");
        if (UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration()).endsWith(ATTRIBUTE_STORE_SUFFIX)) {
            throw new UserStoreException("This is an attribute store", "Can't authenticate ");
        }
        return true;
    }

    @Override
    public boolean doPreAuthenticateWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        log.info("Custom Attribute Store , doPreAuthenticateWithID method");
        if (UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration()).endsWith(ATTRIBUTE_STORE_SUFFIX)) {
            throw new UserStoreException("This is an attribute store", "Can't authenticate ");
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValuesWithID
            (String userID, Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        log.info("Custom Attribute Store , doPreSetUserClaimValuesWithID method");
        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        log.info("domain name" + domainName);
        if (!domainName.endsWith(ATTRIBUTE_STORE_SUFFIX)) {
            String attributeStoreDomain = domainName + ATTRIBUTE_STORE_SUFFIX;
            RealmService realmService = CustomAttributeStoreComponent.getRealmService();
            if (realmService != null) {
                UserRealm userRealm = realmService.getUserRealm(userStoreManager.getRealmConfiguration());
                UserStoreManager secondaryUserStoreManager = userRealm.getUserStoreManager().
                        getSecondaryUserStoreManager(attributeStoreDomain);
                if (secondaryUserStoreManager != null) {
                    UserUniqueIDManger uniqueIDManger = new UserUniqueIDManger();
                    User user = uniqueIDManger.getUser(userID, (AbstractUserStoreManager) secondaryUserStoreManager);
                    userStoreManager = secondaryUserStoreManager;
                    userStoreManager.setUserClaimValues(user.getUsername(), claims, profileName);
                } else {
                    log.info(" secondary user store manager is null for " + attributeStoreDomain);
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues
            (String userName, Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        log.info("Custom Attribute Store , doPreSetUserClaimValues method");
        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        log.info("domain name" + domainName);
        if (!domainName.endsWith(ATTRIBUTE_STORE_SUFFIX)) {
            String attributeStoreDomain = domainName + ATTRIBUTE_STORE_SUFFIX;
            RealmService realmService = CustomAttributeStoreComponent.getRealmService();
            if (realmService != null) {
                UserRealm userRealm = realmService.getUserRealm(userStoreManager.getRealmConfiguration());
                UserStoreManager secondaryUserStoreManager = userRealm.getUserStoreManager().
                        getSecondaryUserStoreManager(attributeStoreDomain);
                if (secondaryUserStoreManager != null) {
                    userStoreManager = secondaryUserStoreManager;
                    userStoreManager.setUserClaimValues(userName, claims, profileName);
                } else {
                    log.info(" secondary user store manager is null for " + attributeStoreDomain);
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
                                            Map<String, String> claimMap, UserStoreManager storeManager)
            throws UserStoreException {

        log.info("Custom Attribute Store , doPostGetUserClaimValues method");
        String userDomainName = UserCoreUtil.getDomainName(storeManager.getRealmConfiguration());
        String attributeStoreDomain;
        if (!StringUtils.containsIgnoreCase(userDomainName, ATTRIBUTE_STORE_SUFFIX)) {
            attributeStoreDomain = userDomainName + ATTRIBUTE_STORE_SUFFIX;
            RealmService userRealmService = CustomAttributeStoreComponent.getRealmService();
            if (userRealmService != null && storeManager.getRealmConfiguration() != null) {
                UserRealm userRealm = userRealmService.getUserRealm(storeManager.getRealmConfiguration());
                UserStoreManager userStoreManager = userRealm.getUserStoreManager().
                        getSecondaryUserStoreManager(attributeStoreDomain);
                if (userStoreManager != null && userStoreManager.isExistingUser(userName)) {
                    Map<String, String> newClaimMap = userStoreManager.
                            getUserClaimValues(userName, claims, profileName);
                    claimMap.clear();
                    claimMap.putAll(newClaimMap);
                    log.info(claimMap);
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
                                                  Map<String, String> claimMap, UserStoreManager userStoreManager)
            throws UserStoreException {

        log.info("Custom Attribute Store , doPostGetUserClaimValuesWithID method");
        String userDomainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        String attributeStoreDomain;
        if (!StringUtils.containsIgnoreCase(userDomainName, ATTRIBUTE_STORE_SUFFIX)) {
            attributeStoreDomain = userDomainName + ATTRIBUTE_STORE_SUFFIX;
            RealmService userRealmService = CustomAttributeStoreComponent.getRealmService();
            if (userRealmService != null && userStoreManager.getRealmConfiguration() != null) {
                UserRealm userRealm = userRealmService.getUserRealm(userStoreManager.getRealmConfiguration());
                UserStoreManager storeManager = userRealm.getUserStoreManager().
                        getSecondaryUserStoreManager(attributeStoreDomain);
                UserUniqueIDManger uniqueIDManger = new UserUniqueIDManger();
                User user = uniqueIDManger.getUser(userID, (AbstractUserStoreManager) storeManager);
                if (storeManager != null && userStoreManager.isExistingUser(user.getUsername())) {
                    Map<String, String> newClaimMap = storeManager.
                            getUserClaimValues(user.getUsername(), claims, profileName);
                    claimMap.clear();
                    claimMap.putAll(newClaimMap);
                    log.info(claimMap);
                }
            }
        }
        return true;
    }

}