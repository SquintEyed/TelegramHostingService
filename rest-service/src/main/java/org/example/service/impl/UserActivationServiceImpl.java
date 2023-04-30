package org.example.service.impl;

import org.example.dao.AppUserDao;
import org.example.entity.AppUser;
import org.example.service.UserActivationService;
import org.example.utils.CryptoTool;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserActivationServiceImpl implements UserActivationService {

    private final AppUserDao appUserDao;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDao appUserDao, CryptoTool cryptoTool) {
        this.appUserDao = appUserDao;
        this.cryptoTool = cryptoTool;
    }


    @Override
    public boolean activate(String cryptoUserId) {

        Long userId = cryptoTool.idOf(cryptoUserId);

        Optional<AppUser> optionalAppUser = appUserDao.findById(userId);

        if (optionalAppUser.isPresent()) {

           AppUser user = optionalAppUser.get();

           user.setActive(true);

           appUserDao.save(user);

           return true;
        }

        return false;
    }
}
