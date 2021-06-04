package com.wso2.carbon.custom.attribute.store;


import com.wso2.carbon.custom.attribute.store.internal.CustomAttributeStoreComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.common.UserUniqueIDManger;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CustomAttributeStoreAuditLogger extends AbstractIdentityUserOperationEventListener {

    private static final String ATTRIBUTE_STORE_SUFFIX = "-ATTRIBUTE-STORE";
    private static Log log = LogFactory.getLog(CustomAttributeStoreAuditLogger.class);

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
                if (storeManager != null) {
                    UserUniqueIDManger uniqueIDManger = new UserUniqueIDManger();
                    User user = uniqueIDManger.getUser(userID, (AbstractUserStoreManager) storeManager);
                    if (userStoreManager.isExistingUser(user.getUsername())) {
                        Map<String, String> newClaimMap = storeManager.
                                getUserClaimValues(user.getUsername(), claims, profileName);
                        claimMap.clear();
                        claimMap.putAll(newClaimMap);
                        log.info(claimMap);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPostAddUserWithID(User user, Object credential, String[] roleList,
                                       Map<String, String> claims, String profile, UserStoreManager userStoreManager)
            throws UserStoreException {
        log.info("Custom Attribute Store , doPostAddUserWithID method");
        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        if (!domainName.endsWith(ATTRIBUTE_STORE_SUFFIX)) {
            String attributeStoreDomain = domainName + ATTRIBUTE_STORE_SUFFIX;
            RealmService realmService = CustomAttributeStoreComponent.getRealmService();
            if (realmService != null) {
                UserRealm userRealm = realmService.getUserRealm(userStoreManager.getRealmConfiguration());
                UserStoreManager secondaryUserStoreManager = userRealm.getUserStoreManager().
                        getSecondaryUserStoreManager(attributeStoreDomain);
                if (secondaryUserStoreManager != null) {
                    userStoreManager = secondaryUserStoreManager;
                    userStoreManager.addUser(user.getUsername(), credential.toString(), roleList, claims, profile);
                    userStoreManager.setUserClaimValues(user.getUsername(), claims, profile);
                } else {
                    log.info(" secondary user store manager is null for " + attributeStoreDomain);
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {
        log.info("Custom Attribute Store , doPostDeleteUserWithID method");
        String domainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        if (!domainName.endsWith(ATTRIBUTE_STORE_SUFFIX)) {
            String attributeStoreDomain = domainName + ATTRIBUTE_STORE_SUFFIX;
            RealmService realmService = CustomAttributeStoreComponent.getRealmService();
            if (realmService != null) {
                UserRealm userRealm = realmService.getUserRealm(userStoreManager.getRealmConfiguration());
                UserStoreManager secondaryUserStoreManager = userRealm.getUserStoreManager().
                        getSecondaryUserStoreManager(attributeStoreDomain);
                if (secondaryUserStoreManager != null) {
                    UserUniqueIDManger uniqueIDManger = new UserUniqueIDManger();
                    User user = uniqueIDManger.getUser(userID, (AbstractUserStoreManager) userStoreManager);
                    if (user != null) {
                        userStoreManager = secondaryUserStoreManager;
                        userStoreManager.deleteUser(user.getUsername());
                    }
                } else {
                    log.info(" secondary user store manager is null for " + attributeStoreDomain);
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, final List<User> returnValues,
                                           UserStoreManager userStoreManager)
            throws UserStoreException {
        returnValues.removeIf(user -> user.getUserStoreDomain().contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }

    @Override
    public boolean doPostGetUserList(String claimUri, String claimValue, final List<String> returnValues,
                                     UserStoreManager userStoreManager)
            throws UserStoreException {
        returnValues.removeIf(user -> user.contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }

    @Override
    public boolean doPostListUsers(String filter, int limit, int offset, List<String> returnValues,
                                   UserStoreManager userStoreManager) throws UserStoreException {

        returnValues.removeIf(user -> user.contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, List<User> returnValues,
                                           int limit, int offset, UserStoreManager userStoreManager)
            throws UserStoreException {

        returnValues.removeIf(user -> user.getUserStoreDomain().contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(Condition condition, String domain, String profileName,
                                           int limit, int offset, String sortBy, String sortOrder, List<User> users,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        users.removeIf(user -> user.getUserStoreDomain().contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }

    @Override
    public boolean doPostListUsersWithID(String filter, int limit, int offset, List<User> returnValues,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        returnValues.removeIf(user -> user.getUserStoreDomain().contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }

    @Override
    public boolean doPostGetUserList(String claimUri, String claimValue, List<String> returnValues,
                                     int limit, int offset, UserStoreManager userStoreManager)
            throws UserStoreException {

        returnValues.removeIf(user -> user.contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }

    @Override
    public boolean doPostGetUserList(Condition condition, String domain,
                                     String profileName, int limit, int offset, String sortBy, String sortOrder,
                                     String[] users, UserStoreManager userStoreManager) throws UserStoreException {

        Arrays.asList(users).removeIf(user -> user.contains(ATTRIBUTE_STORE_SUFFIX));
        return true;
    }
}
