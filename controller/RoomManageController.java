package com.sist.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sist.web.model.AccommodationRoom;
import com.sist.web.service.RoomManageService;

@Controller("accommRoomManageController")
public class RoomManageController {
    private static Logger logger = LoggerFactory.getLogger(RoomManageController.class);
    
      @Value("#{env['upload.save.dir.accomm']}")
      private String UPLOAD_SAVE_DIR_ROOM;
   
      @Autowired
       private RoomManageService roomManageService;
       
        @RequestMapping(value = "/accomm/accommRoomRegForm", method = RequestMethod.GET)
        public String sellerInfo(HttpServletRequest request, Model model)
        {
            String accommId = request.getParameter("accommId");  // 숙소 ID 받기
            model.addAttribute("accommId", accommId);            // 뷰에서 사용할 수 있도록 모델에 담기
            return "/accomm/accommRoomRegForm";
        }

       // 객실 등록 처리
          @RequestMapping(value = "/seller/roomAdd", method = RequestMethod.POST)
          public String insertRoom(
              @RequestParam("roomImageFile") MultipartFile roomImageFile,
              AccommodationRoom room,
              HttpSession session,
              HttpServletRequest request,
              Model model
          ) {
              try {
                  // 1. 로그인한 사용자 ID 확인
                  String sellerId = (String) session.getAttribute("sellerId");
                  if (sellerId == null) {
                      model.addAttribute("msg", "로그인이 필요합니다.");
                      return "/user/login";
                  }
               // 2.1 객실명 중복 체크

                  if (roomManageService.isRoomNameDuplicate(room.getAccommId(), room.getRoomName())) {
                      model.addAttribute("msg", "이미 동일한 객실명이 존재합니다.");
                      return "/seller/roomAdd";
                  }
                  logger.debug("객실 등록 요청 - accommId: {}, roomName: {}", room.getAccommId(), room.getRoomName());
                  
                  // 2. 이미지 저장 처리
                  if (roomImageFile != null && !roomImageFile.isEmpty()) {
                      String origin = roomImageFile.getOriginalFilename();
                      String ext = origin.substring(origin.lastIndexOf('.'));
                      String saveName = "room_" + System.currentTimeMillis() + ext;

                      File dir = new File(UPLOAD_SAVE_DIR_ROOM);
                      if (!dir.exists()) {
                          dir.mkdirs();
                      }

                      File dest = new File(dir, saveName);
                      roomImageFile.transferTo(dest);

                      // DB에는 웹 경로로 저장
                      room.setRoomImage("/resources/accomm/" + saveName);
                      
                      logger.debug("객실 이미지 저장 완료: {}", dest.getAbsolutePath());
                  }
                  
                  // 3. 편의시설 Y/N 처리
                  room.setBathroom(room.getBathroom() != null ? "Y" : "N");
                  room.setBath(room.getBath() != null ? "Y" : "N");
                  room.setTv(room.getTv() != null ? "Y" : "N");
                  room.setPc(room.getPc() != null ? "Y" : "N");
                  room.setInternet(room.getInternet() != null ? "Y" : "N");
                  room.setRefrigerator(room.getRefrigerator() != null ? "Y" : "N");
                  room.setSofa(room.getSofa() != null ? "Y" : "N");
                  room.setTable(room.getTable() != null ? "Y" : "N");
                  room.setDryer(room.getDryer() != null ? "Y" : "N");
                  
                  // 4. DB 등록
                  roomManageService.insertRoom(room);
                  logger.debug("객실 등록 완료");
                  
                  return "redirect:/seller/roomList?accommId=" + room.getAccommId();
                  
              } catch (Exception e) {
                  logger.error("객실 등록 오류", e);
                  model.addAttribute("msg", "객실 등록 중 오류: " + e.getMessage());
                  return "/error";
              }
          }
       
       @RequestMapping(value = "/seller/roomList", method = RequestMethod.GET)
       public String roomList(
               @RequestParam("accommId") String accommId,
               Model model,
               HttpSession session
       ) {
           String sellerId = (String) session.getAttribute("sellerId");
           if (sellerId == null) return "redirect:/user/login";

           logger.debug("객실 리스트 조회 - accommId: {}", accommId);

           if (accommId == null || accommId.trim().isEmpty()) {
               logger.warn("accommId가 null 또는 빈 문자열입니다.");
               model.addAttribute("errorMsg", "숙소 ID가 올바르지 않습니다.");
               return "/error";
           }

           try {
               List<AccommodationRoom> roomList = roomManageService.findRoomsByAccomm(accommId);
               logger.debug("조회된 객실 수: {}", roomList != null ? roomList.size() : 0);

               model.addAttribute("roomList", roomList);
               model.addAttribute("accommId", accommId);  // 객실 추가 버튼에서 사용

           } catch (Exception e) {
               logger.error("객실 리스트 조회 실패", e);
               model.addAttribute("errorMsg", "객실 리스트 조회 중 오류가 발생했습니다.");
               return "/error";
           }

           return "/seller/roomList";
       }
       @RequestMapping(value = "/seller/roomDelete", method = RequestMethod.GET)
       public String deleteRoom(@RequestParam("roomId") String roomId,
                                @RequestParam("accommId") String accommId,
                                HttpSession session, Model model) {
           String sellerId = (String) session.getAttribute("sellerId");
           if (sellerId == null) {
               return "redirect:/user/login";
           }

           try {
               roomManageService.deleteRoom(roomId);
               return "redirect:/seller/roomList?accommId=" + accommId;
           } catch (Exception e) {
               model.addAttribute("msg", "객실 삭제 중 오류 발생: " + e.getMessage());
               return "/error";
           }
       }
}