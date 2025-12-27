package com.sky.service;


import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressService {


    void save(AddressBook addressBook);

    List<AddressBook> list();
}
