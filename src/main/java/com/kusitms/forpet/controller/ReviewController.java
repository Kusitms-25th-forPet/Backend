package com.kusitms.forpet.controller;

import com.kusitms.forpet.domain.Review;
import com.kusitms.forpet.dto.ReviewDto;
import com.kusitms.forpet.dto.ReviewRequestDto;
import com.kusitms.forpet.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    //리뷰생성
    @PostMapping("/offline-map/{placeid}/review")
    public Long createReviewByPlaceId(@PathVariable("placeid") Long placeid,
                                      @RequestPart(value = "reviewRequestDto") ReviewRequestDto requestDto,
                                      @RequestPart(value = "imageList") List<MultipartFile> multipartFile) {
        /**
         * 작성회원 찾기
         */
        //User user = userRepository.findById();
        //String nickName = user.getNickName();
        ////////////프로필 이미지 처리//////////////

        Long id = reviewService.createReviewByPlaceId(placeid, requestDto.getStar(), requestDto.getContent(),
                requestDto.getWriter(), multipartFile);

        return id;
    }


    //마커 선택(리뷰 정보)
    @GetMapping("offline-map/{placeid}/marker/review")
    public List<ReviewDto> getReviewByMarker(@PathVariable("placeid") Long placeid) {
        List<Review> list = reviewService.findReviewByPlaceId(placeid);

        //entity -> dto 변환
        List<ReviewDto> collect = list.stream().map(m -> new ReviewDto(m.getId(), m.getStar(), m.getContent(),
                        m.getWriter(), m.getCreateDate(), m.getImageUrlList().split("#")))
                .collect(Collectors.toList());

        return collect;
    }


}

