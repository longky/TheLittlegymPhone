package com.thelittlegym.mobile.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thelittlegym.mobile.common.OasisService;
import com.thelittlegym.mobile.entity.*;
import com.thelittlegym.mobile.enums.ResultEnum;
import com.thelittlegym.mobile.exception.MyException;
import com.thelittlegym.mobile.service.IFeedbackService;
import com.thelittlegym.mobile.service.ILoginService;
import com.thelittlegym.mobile.service.IUserService;
import com.thelittlegym.mobile.utils.ResultUtil;
import com.thelittlegym.mobile.utils.msg.config.AppConfig;
import com.thelittlegym.mobile.utils.msg.lib.MESSAGEXsend;
import com.thelittlegym.mobile.utils.msg.utils.ConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/login")
public class LoginCtrl {
    private static AppConfig config = ConfigLoader.load(ConfigLoader.ConfigType.Message);
    @Autowired
    private ILoginService loginService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IFeedbackService feedbackService;
    @Autowired
    private OasisService oasisService;


    @PostMapping("/login")
    @ResponseBody
    public Result login(HttpServletRequest request, String username, String password) {

        Result res = loginService.login(username, password);


        if (res != null) {
            //获取user实体
            User user = (User) res.getData();
            HttpSession session = request.getSession(true);
            Object objSession = session.getAttribute("user");
            //重复登录清空之前session所有attr
            if ( null != objSession ){
                Enumeration<String> em = session.getAttributeNames();
                while (em.hasMoreElements()) {
                    String sessionStr = em.nextElement();
                    if (!"admin".equals(sessionStr)){
                        session.removeAttribute(sessionStr);
                    }
                }
            }
            session.setAttribute("user", user);
        }
        return res;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public Result register(HttpServletRequest request, String username, String valnum, String password, String email) {

        HttpSession session = request.getSession();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map valNumMap = new HashMap();
        //验证码校验
        if (session.getAttribute("valNumMap") != null) {
            valNumMap = (HashMap) session.getAttribute("valNumMap");
            if (valNumMap.get("valNum").equals(valnum) == false) {
                throw new MyException(ResultEnum.CHECKSUM_WRONG);
            }
            long minsPass = getDateDiffMins((Date) valNumMap.get("valTimeStamp"), new Date());
            if (minsPass > 30) {
                throw new MyException(ResultEnum.CHECKSUM_OVERDUE);
            }
        } else {
            throw new MyException(ResultEnum.CHECKSUM_WRONG);
        }


        try {
            String sqlExist = "select top 1 crmzdy_81778300 gym,crmzdy_81486367 city,crmzdy_80610671 addr,crm_surname name,id,crmzdy_80620120 tel,crmzdy_81802271 childname,crmzdy_81778300 zx from crm_sj_238592_view  where charindex('" +username+"',crmzdy_81767199)>0";

            JSONArray jsonArray = oasisService.getResultJson(sqlExist);
            //是否是会员校验
            if (jsonArray == null){
                    throw new MyException(ResultEnum.REGISTER_NOT_ALLOW);
            }

            JSONObject familyObj = jsonArray.getJSONObject(0);
            Family family = JSONObject.toJavaObject(familyObj,Family.class);

            return  loginService.register(username, password,email,family);

        } catch (Exception e) {
            throw e;
        }

    }


    @RequestMapping(value = "/updatePass", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updatePass(HttpServletRequest request, String tel, String newPass, String valNum) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        HttpSession session = request.getSession();
        Map<String,Object> valNumMap = new HashMap<String,Object>();
        //验证码校验
        if (session.getAttribute("valNumMap") != null) {
            valNumMap = (HashMap) session.getAttribute("valNumMap");
            if (valNumMap.get("valNum").equals(valNum) == false) {
                returnMap.put("message", "验证码错误！");
                returnMap.put("success", false);
                return returnMap;
            }
            long minsPass = getDateDiffMins((Date) valNumMap.get("valTimeStamp"), new Date());
            if (minsPass > 30) {
                returnMap.put("message", "验证码已过期！");
                returnMap.put("success", false);
                return returnMap;
            }
            try {
                //修改密码
                User u = userService.getByTel(tel);
//                newPass = getHash(newPass,"MD5");
                u.setPassword(newPass);
                userService.updateUser(u);
                returnMap.put("message", "修改成功！");
                returnMap.put("success", true);
                session.setAttribute("valNumMap",null);
                return returnMap;
            } catch (Exception e) {
                returnMap.put("message", "异常错误！");
                returnMap.put("success", false);
                return returnMap;
            }

        } else {
            returnMap.put("message", "验证码错误!");
            returnMap.put("success", false);
            return returnMap;
        }
    }


    @RequestMapping(value = "/getUserPageListForSearch", method = RequestMethod.POST)
    @ResponseBody
    public Result getUserPageListForSearch(HttpServletRequest request,
                                                        @RequestParam(value = "pageNow", defaultValue = "1") Integer pageNow,
                                                        @RequestParam(value = "size", defaultValue = "10") Integer size, String blurUserName) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Sort sort = new Sort(Sort.Direction.ASC, "id");
        Pageable pageable = new PageRequest(pageNow, size, sort);
        try {
            Page<User> userPages = userService.getUserPageListForSearch(blurUserName,pageable);
            return ResultUtil.success(userPages);
        } catch (Exception e) {
            //继续外抛系统错误，后续会处理
            throw e;
        }
    }

    @RequestMapping(value = "/validateNum", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> validateNum(HttpServletRequest request, String tel) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map valnumMap = new HashMap();
        String valnum = getRandomStr(1000, 9999);
        HttpSession session = request.getSession();

        MESSAGEXsend submail = new MESSAGEXsend(config);
        submail.addTo(tel);
        submail.setProject("IkkGR1");
        submail.addVar("time", "30分钟");
        submail.addVar("code", valnum);
        submail.xsend();

        try {
            //验证码map
            valnumMap.put("valNum", valnum);
            valnumMap.put("valTimeStamp", new Date());

            session.setAttribute("valNumMap", valnumMap);

            returnMap.put("message", valnum);
            returnMap.put("success", true);
        } catch (Exception e) {
            returnMap.put("message", "异常：发送失败!");
            returnMap.put("success", false);
            e.printStackTrace();
        }
        return returnMap;
    }

    @RequestMapping(value = "/deleteOneUser", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteOneUser(HttpServletRequest request, Integer id) {

        return  userService.deleteOneUser(id);

    }

    @RequestMapping(value = "/getUserPageList", method = RequestMethod.POST)
    @ResponseBody
    public Result getUserPageList(HttpServletRequest request,
                @RequestParam(value = "pageNow", defaultValue = "1") Integer pageNow,
                @RequestParam(value = "size", defaultValue = "10") Integer size ) {

            Sort sort = new Sort(Sort.Direction.ASC, "id");
            Pageable pageable = new PageRequest(pageNow, size, sort);
            try {
                Page<User> userPages = userService.getUserPageList(pageable);
                return ResultUtil.success(userPages);
            } catch (Exception e) {
                throw e;
            }
        }


    @RequestMapping(value = "/exist", method = RequestMethod.POST)
    @ResponseBody
    public Result exist(String telephone) {
        try {
            if (userService.isReged(telephone) ){
                throw new MyException(ResultEnum.REGISTER_USER_EXIST);
            }
        } catch (Exception e) {
                throw e;
        }
        String sqlExist = "select top 1 crm_surname name,id,crmzdy_80620120 tel,crmzdy_81802271 childname,crmzdy_81778300 zx from   crm_sj_238592_view  where charindex('" +telephone+"',crmzdy_81767199)>0";
        if (oasisService.getResultJson(sqlExist) != null){
            return ResultUtil.success(ResultEnum.REGISTER_ALLOW);
        }else{
            return ResultUtil.success(ResultEnum.REGISTER_NOT_ALLOW);
        }

    }

    @RequestMapping(value = "/exist_reged", method = RequestMethod.POST)
    @ResponseBody
    public Result exist_reged(String telephone) {

        try {
            User u = userService.getByTel(telephone);
            if ( null != u){
                return ResultUtil.success(ResultEnum.IS_REGISTERED);
            }else{
                throw new MyException(ResultEnum.IS_NOT_REGISTERED);
            }
        } catch (Exception e) {
            throw e;
        }

    }

    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
    @ResponseBody
    public  String feedback(String Franchisee,String name,String details,String contactTel,String type) {
        log.info(contactTel);
        String result;
        Feedback feedback = new Feedback();
        try {
            if( null == contactTel || "".equals(contactTel)){
                throw new MyException(ResultEnum.VALIDATE_TEL);
            }
            if( null == type || "".equals(type)){
                throw new MyException(ResultEnum.VALIDATE_TYPE);
            }
            if( null == name || "".equals(name)){
                throw new MyException(ResultEnum.VALIDATE_NAME);
            }

            feedback.setFranchisee(Franchisee);
            feedback.setCreateTime(new Date());
            feedback.setHandled(false);
            feedback.setContactTel(contactTel);
            feedback.setName(name);
            feedback.setDetails(details);
            feedback.setType(type);
            Feedback res = feedbackService.save(feedback);
            if (res!=null) {
                result = ResultEnum.FEEDBACK_SUCCESS.toJson();
            }else {
                result = ResultEnum.FEEDBACK_FAILURE.toJson();
            }
        } catch (Exception e) {
            throw  e;
        }
        return result;
    }


    /*
    生成随机数验证码
     */
    public String getRandomStr(int min, int max) {
        int randNum = min + (int) (Math.random() * ((max - min) + 1));
        return randNum + "";
    }

    /*
    时间比较，分钟
     */
    public long getDateDiffMins(Date initDate, Date nowDate) {
        long nm = 1000 * 60;
        long diff = nowDate.getTime() - initDate.getTime();
        long mins = diff / nm;
        return mins;
    }


    /*
    md5加密
     */
    public static String getHash(String source, String hashType) {
        // 用来将字节转换成 16 进制表示的字符
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest md = MessageDigest.getInstance(hashType);
            md.update(source.getBytes()); // 通过使用 update 方法处理数据,使指定的 byte数组更新摘要
            byte[] encryptStr = md.digest(); // 获得密文完成哈希计算,产生128 位的长整数
            char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符
            int k = 0; // 表示转换结果中对应的字符位置
            for (int i = 0; i < 16; i++) { // 从第一个字节开始，对每一个字节,转换成 16 进制字符的转换
                byte byte0 = encryptStr[i]; // 取第 i 个字节
                str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移
                str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
            }
            return new String(str); // 换后的结果转换为字符串
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}