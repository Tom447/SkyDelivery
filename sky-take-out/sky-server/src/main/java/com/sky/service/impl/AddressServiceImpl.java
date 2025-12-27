package com.sky.service.impl;


import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressMapper;
import com.sky.service.AddressService;
import com.sky.utils.BeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {


    @Autowired
    private AddressMapper addressMapper;

    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setCreateTime(LocalDateTime.now());

        //查找地址簿中是否含有默认的地址
        AddressBook defaultAddressBook = addressMapper.getDefaultAddressBook();

        if (!Objects.isNull(defaultAddressBook)){
            //有默认地址
            addressBook.setIsDefault(StatusConstant.DISABLE);
        }else{
            //没有默认地址
            addressBook.setIsDefault(StatusConstant.ENABLE);
        }
        addressMapper.save(addressBook);
    }
}
