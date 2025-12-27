package com.sky.mapper;


import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AddressMapper {

    /**
     * 保存地址簿
     * @param addressBook
     */
    void save(AddressBook addressBook);


    @Select("select * from address_book where is_default = 1")
    AddressBook getDefaultAddressBook();
}

