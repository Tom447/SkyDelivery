package com.sky.test;


import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@Slf4j
public class PoiTest {

    /**
     * 解析 C:\Users\11423\Desktop\now_using_front_End\sky_delivery\1.xlsx
     *
     */
    @Test
    public void testReadExcel() throws Exception {
        //1.构建一个workbook
        FileInputStream inputStream = new FileInputStream(new File("C:\\Users\\11423\\Desktop\\now_using_front_End\\sky_delivery\\1.xlsx"));
        Workbook workbook = new XSSFWorkbook(inputStream);

        //2.定位要操作的sheet对象
        Sheet sheet = workbook.getSheetAt(0);
        //表格中实际的行数
        int rowNums = sheet.getPhysicalNumberOfRows();
        //3.遍历其中的每一行
        for (int i = 0; i < rowNums; i++) {
            Row row = sheet.getRow(i);
            //获取这一行实际的单元格数
            int cellsNum = row.getPhysicalNumberOfCells();
            //获取每一行中每一个单个值，输出
            StringBuffer buffer = new StringBuffer();
            //4.遍历每一行中的每一个单个的值，输出
            for (int j = 0; j < cellsNum; j++) {
                Cell cell = row.getCell(j);
                //4.1 获取单元格的数字类型
                CellType cellType = cell.getCellType();
                //4.2 根据类型，来调用对应的方法获取单元格的值
                // 4.2 根据类型，来调用对应的方法获取单元格的值
                switch (cellType) {
                    case NUMERIC: // 数字
                        buffer.append(cell.getNumericCellValue()).append("   ");
                        break;
                    case STRING: // 字符串
                        buffer.append(cell.getStringCellValue()).append("   ");
                        break;
                    case ERROR: // 错误
                        buffer.append(cell.getErrorCellValue()).append("   ");
                        break;
                    case BOOLEAN: // 布尔
                        buffer.append(cell.getBooleanCellValue()).append("   ");
                        break;
                    default:
                        buffer.append(cell.getStringCellValue()).append("   ");
                        break;
                }
            }
            //输出
            System.out.println(buffer.toString());
        }

        //5.释放资源
        workbook.close();
        inputStream.close();
    }

    //生成一个Excel的文件xlsx
    @Test
    public void writeExcel() throws IOException {
        //1. 创建一个workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        //2. 创建对应的sheet
        Sheet sheet = workbook.createSheet("sheetTest1");
        //设置一下格式
        sheet.setDefaultColumnWidth(20);//列宽
        sheet.setDefaultRowHeight((short) 700);//行高
        //样式
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        
        //3. 创建row -- 表头
        Row row0 = sheet.createRow(0);

        //4.创建cell填充数据 --表头单元格，填充数据
        Cell cell0 = row0.createCell(0);  cell0.setCellStyle(cellStyle);  cell0.setCellValue("编号");
        Cell cell1 = row0.createCell(1);  cell1.setCellStyle(cellStyle);    cell1.setCellValue("姓名");
        Cell cell2 = row0.createCell(2);  cell2.setCellStyle(cellStyle);    cell2.setCellValue("性别");
        Cell cell3 = row0.createCell(3);  cell3.setCellStyle(cellStyle);    cell3.setCellValue("年龄");


        //5.创建row -内容
        for (int i = 1; i <= 10; i++){
            Row row = sheet.createRow(i);
            Cell _cell0 = row.createCell(0); _cell0.setCellValue(i); _cell0.setCellStyle(cellStyle);
            Cell _cell1 = row.createCell(1); _cell1.setCellValue("张" + i); _cell1.setCellStyle(cellStyle);
            Cell _cell2 = row.createCell(2); _cell2.setCellValue(i%2 == 0? "男":"女"); _cell2.setCellStyle(cellStyle);
            Cell _cell3 = row.createCell(3); _cell3.setCellValue(10 + i); _cell3.setCellStyle(cellStyle);
        }

        //6.写入文件
        FileOutputStream out = new FileOutputStream("C:\\Users\\11423\\Desktop\\now_using_front_End\\sky_delivery\\test2.xlsx");
        workbook.write(out);

        //7.释放资源
        workbook.close();
        out.close();
    }
}
