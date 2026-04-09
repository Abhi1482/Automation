package com.saas.analytics.service;

import com.saas.analytics.model.IntegrationMeta;
import com.saas.analytics.model.IntegrationShopify;
import com.saas.analytics.model.User;
import com.saas.analytics.repository.IntegrationMetaRepository;
import com.saas.analytics.repository.IntegrationShopifyRepository;
import com.saas.analytics.util.AESUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntegrationService {

    private final IntegrationMetaRepository metaRepository;
    private final IntegrationShopifyRepository shopifyRepository;
    private final AESUtil aesUtil;

    public IntegrationService(IntegrationMetaRepository metaRepository, 
                              IntegrationShopifyRepository shopifyRepository, 
                              AESUtil aesUtil) {
        this.metaRepository = metaRepository;
        this.shopifyRepository = shopifyRepository;
        this.aesUtil = aesUtil;
    }

    public void addMetaAccount(User user, String accountName, String adAccountId, String accessToken, String currency) {
        IntegrationMeta meta = new IntegrationMeta();
        meta.setUser(user);
        meta.setAccountName(accountName);
        meta.setAdAccountId(adAccountId);
        meta.setAccessToken(aesUtil.encrypt(accessToken));
        meta.setCurrency(currency);
        metaRepository.save(meta);
    }

    public void addShopifyStore(User user, String storeName, String storeUrl, String clientId, String clientSecret) {
        IntegrationShopify shopify = new IntegrationShopify();
        shopify.setUser(user);
        shopify.setStoreName(storeName);
        shopify.setStoreUrl(storeUrl);
        shopify.setClientId(clientId);
        shopify.setClientSecret(aesUtil.encrypt(clientSecret));
        shopifyRepository.save(shopify);
    }
    
    public List<IntegrationMeta> getUserMetaAccounts(Long userId) {
        return metaRepository.findAllByUserId(userId);
    }
    
    public List<IntegrationShopify> getUserShopifyStores(Long userId) {
        return shopifyRepository.findAllByUserId(userId);
    }
    
    public void deleteMetaAccount(Long id, Long userId) {
        metaRepository.findById(id).filter(m -> m.getUser().getId().equals(userId)).ifPresent(metaRepository::delete);
    }

    public void deleteShopifyStore(Long id, Long userId) {
        shopifyRepository.findById(id).filter(s -> s.getUser().getId().equals(userId)).ifPresent(shopifyRepository::delete);
    }
}
