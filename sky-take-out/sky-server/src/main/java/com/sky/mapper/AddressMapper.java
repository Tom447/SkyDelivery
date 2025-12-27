package com.sky.mapper;


import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressMapper {



    /**
     * 保存地址簿
     * @param addressBook
     */
    void save(AddressBook addressBook);


    @Select("select * from address_book where is_default = 1")
    AddressBook getDefaultAddressBook();

    @Select("select * from address_book")
    List<AddressBook> list();

    @Select("select * from address_book where id = #{id}")
    AddressBook getAddressById(Long id);

    void update(AddressBook addressBook);


    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);
}

