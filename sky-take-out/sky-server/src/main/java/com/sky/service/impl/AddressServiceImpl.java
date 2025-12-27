package com.sky.service.impl;


import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.BusinessException;
import com.sky.mapper.AddressMapper;
import com.sky.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public List<AddressBook> list() {
        return addressMapper.list();
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id) {
        //根据id查找到地址簿addressbook
        AddressBook addressBook = addressMapper.getAddressById(id);
        if (Objects.isNull(addressBook)){
            //该id的地址不存在
            throw new BusinessException("没有id为{ + "+ id+ "+ }的地址");
        }else{
            //该id的地址存在
            //1.先查找到当前的默认的地址
            AddressBook defaultAddressBook = addressMapper.getDefaultAddressBook();
            if (!Objects.isNull(defaultAddressBook)){//有默认地址
                //2.将当前的默认的地址设置为非默认地址
                defaultAddressBook.setIsDefault(StatusConstant.DISABLE);
                addressMapper.update(defaultAddressBook);
                //3.将id的地址簿设置为默认地址
                addressBook.setIsDefault(StatusConstant.ENABLE);
                addressMapper.update(addressBook);
            }else {//无默认地址
                addressBook.setIsDefault(StatusConstant.ENABLE);
                addressMapper.update(addressBook);
            }
        }
    }

    @Override
    public AddressBook getdefaultAddress() {
        AddressBook defaultAddressBook = addressMapper.getDefaultAddressBook();
        if (!Objects.isNull(defaultAddressBook)){
            return defaultAddressBook;
        }else {
            throw new BusinessException("没有默认收货地址");
        }
    }

    @Override
    public AddressBook getInfoById(Long id) {
        return addressMapper.getAddressById(id);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressMapper.update(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        //1.判断id的address是否是默认地址
        AddressBook addressById = addressMapper.getAddressById(id);
        if (!Objects.isNull(addressById)){ //查到了
            //2.如果id的address是默认地址，就提示默认地址不可删除，如果不是就删掉
            if(addressById.getIsDefault() == StatusConstant.ENABLE){
                //是默认地址
                throw new BusinessException("默认地址不可删除");
            }else{
                //不是默认地址
                addressMapper.deleteById(id);
            }
        }else{
            //没查到
            throw new BusinessException("要删除的地址不存在");
        }
    }


}
