package susussg.pengreenlive.broadcast.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import susussg.pengreenlive.broadcast.dto.*;
import susussg.pengreenlive.broadcast.service.LiveRegisterService;

import java.io.IOException;
import java.util.List;

@RestController
@Log4j2
public class LiveRegisterController {

    @Autowired
    private final LiveRegisterService liveRegisterService;

    public LiveRegisterController(LiveRegisterService liveRegisterService) {
        this.liveRegisterService = liveRegisterService;
    }

    //판매자 고유번호
    long vendorId = 2;

    // 방송 카테고리 목록 불러오기
    @GetMapping("/broadcast-category")
    public ResponseEntity<List<BroadcastCategoryDTO>> showBroadcastCategory() {
        List<BroadcastCategoryDTO> categoryList = liveRegisterService.getAllCategory();
        return ResponseEntity.ok().body(categoryList);
    }

    // 방송 등록
    @PostMapping("/live-register")
    public ResponseEntity<?> registerBroadcast(
            @RequestParam(value = "image", required = false) MultipartFile broadcastImage,
            @RequestBody BroadcastRegistrationRequestDTO request) {
        String channelName = liveRegisterService.getChannelName(vendorId); // 판매자 정보

        BroadcastDTO broadcastDTO = BroadcastDTO.builder()
                .channelNm(channelName)
                .broadcastTitle(request.getBroadcastTitle())
                .broadcastSummary(request.getBroadcastSummary())
                .broadcastScheduledTime(request.getBroadcastScheduledTime())
                .categoryCd(request.getCategoryCd())
                .build();

        if (broadcastImage != null && !broadcastImage.isEmpty()) {
            try {
                byte[] imageBytes = broadcastImage.getBytes();
                broadcastDTO.setBroadcastImage(imageBytes);
            } catch (IOException e) {
                return ResponseEntity.badRequest().body("이미지 파일 처리 중 오류 발생: " + e.getMessage());
            }
        }

        liveRegisterService.saveBroadcast(broadcastDTO);

        request.getRegisteredProducts().forEach(productInfo -> {
            BroadcastProductDTO productDTO = BroadcastProductDTO.builder()
                    .broadcastSeq(broadcastDTO.getBroadcastSeq())
                    .productSeq(productInfo.getProductSeq())
                    .discountRate(productInfo.getDiscountRate())
                    .discountPrice(productInfo.getDiscountPrice())
                    .build();
            liveRegisterService.saveBroadcastProduct(productDTO);
            log.info("BroadcastProductDTO: " + productDTO.toString());
        });

        request.getNotices().forEach(notice -> {
            NoticeDTO noticeDTO = NoticeDTO.builder()
                    .broadcastSeq(broadcastDTO.getBroadcastSeq())
                    .noticeContent(notice)
                    .build();
            liveRegisterService.saveNotice(noticeDTO);
        });

        request.getBenefits().forEach(benefit -> {
            BenefitDTO benefitDTO = BenefitDTO.builder()
                    .broadcastSeq(broadcastDTO.getBroadcastSeq())
                    .benefitContent(benefit)
                    .build();
            liveRegisterService.saveBenefit(benefitDTO);
        });

        request.getQa().forEach(qaInfo -> {
            FaqDTO faqDTO = FaqDTO.builder()
                    .broadcastSeq(broadcastDTO.getBroadcastSeq())
                    .questionTitle(qaInfo.getQuestionTitle())
                    .questionAnswer(qaInfo.getQuestionAnswer())
                    .build();
            liveRegisterService.saveFaq(faqDTO);
        });

        return ResponseEntity.ok().body("방송 정보가 성공적으로 등록되었습니다.");
    }


    //채널별 상품 목록
    @GetMapping("/channel-sales-product")
    public ResponseEntity<List<ChannelSalesProductDTO>> showProduct() {
        List<ChannelSalesProductDTO> channelSalesProducts = liveRegisterService.getChannelSalesProductAll(vendorId);
        return ResponseEntity.ok().body(channelSalesProducts);
    }

}
