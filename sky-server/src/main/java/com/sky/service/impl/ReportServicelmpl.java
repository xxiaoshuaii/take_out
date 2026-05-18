package com.sky.service.impl;


import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServicelmpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //创建时间段，将时间进行封装
        List<LocalDate> dateList = new ArrayList<>();
        //遍历每一天的时间
        while ( !end.equals(begin) ) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //创建营业额集合
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //查询当前时间段的营业额
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByDate(map);
            //如果营业额为null，则设置为0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //创建时间段，将时间进行封装
        List<LocalDate> dateList = new ArrayList<>();
        //遍历每一天的时间
        while ( !end.equals(begin) ) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //创建用户总量集合
        List<Integer> totalUserList = new ArrayList<>();
        //创建新用户集合
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            //查询总用户数
            Integer totalUser = userMapper.countByMap(map);
            //查询新用户数
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //创建时间段，将时间进行封装
        List<LocalDate> dateList = new ArrayList<>();
        //遍历每一天的时间
        while ( !end.equals(begin) ) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //创建订单总集合
        List<Integer> orderCountList = new ArrayList<>();
        //创建有效订单集合
        List<Integer> validOrderCountList = new ArrayList<>();
        //订单总数
        Integer totalOrderCount = 0;
        //有效订单数
        Integer validOrderCount = 0;
        //订单完成率
        Double orderCompletionRate = 0.0;

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            //查询总订单数
            Integer orderCount = orderMapper.countByMap(map);
            //添加总订单数
            totalOrderCount += orderCount;
            //查询有效订单数
            map.put("status", Orders.COMPLETED);
            Integer validOrderCountlist = orderMapper.countByMap(map);
            //添加有效订单数
            validOrderCount += validOrderCountlist;
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCountlist);
        }
        //订单完成率
        orderCompletionRate = 0.0;
        if(totalOrderCount != 0)
        {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO gettop(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //封装查询返回参数
        List<GoodsSalesDTO> list = orderDetailMapper.getSalesTop10(beginTime, endTime);
        //利用Stream流获取名称和数量
        List<String> name = list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> number = list.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        //封装返回参数
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(name, ","))
                .numberList(StringUtils.join(number, ","))
                .build();
    }

    /**
     * 导出营业数据
     *  Apache POI
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
     //获取近一个月的时间
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        //调用接口
        BusinessDataVO business = workSpaceService.getBusiness(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));
        //获取输入流
        InputStream in = this.getClass().
                getClassLoader().
                getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //创建Excel表格对象
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            //获取第一个表即sheet页
            XSSFSheet sheet = workbook.getSheet("Sheet1");
            //获取表格中的第二行，起始索引为0
            XSSFRow row = sheet.getRow(1);
            //创建单元格，表格坐标（2，B），填充时间段
            row.getCell(1).setCellValue("时间"+ begin + "至" + end);
            //填充概览数据
            //更改行
            row = sheet.getRow(3);
            //填充数据
            row.getCell(2).setCellValue(business.getTurnover());
            row.getCell(4).setCellValue(business.getOrderCompletionRate());
            row.getCell(6).setCellValue(business.getNewUsers());
            //更改行
            row = sheet.getRow(4);
            //填充数据
            row.getCell(2).setCellValue(business.getValidOrderCount());
            row.getCell(4).setCellValue(business.getUnitPrice());


            //填充明细数据
            begin = begin.plusDays(-1);
            for (int i = 0; i < 30; i++) {
            //获取并且更新日期
            begin = begin.plusDays(1);
                 business = workSpaceService.getBusiness(
                        LocalDateTime.of(begin, LocalTime.MIN),
                        LocalDateTime.of(begin, LocalTime.MAX));
                //获取并且更新行
                row = sheet.getRow(7 + i);
                //填充数据
                row.getCell(1).setCellValue(begin.toString());
                row.getCell(2).setCellValue(business.getTurnover());
                row.getCell(3).setCellValue(business.getValidOrderCount());
                row.getCell(4).setCellValue(business.getOrderCompletionRate());
                row.getCell(5).setCellValue(business.getUnitPrice());
                row.getCell(6).setCellValue(business.getNewUsers());
            }

            //通过输出流将Excel数据写入到浏览器，进行下载
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);

            //关闭流
            out.close();
            workbook.close();
            in.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
