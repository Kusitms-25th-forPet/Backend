package com.kusitms.forpet.controller;

import com.kusitms.forpet.domain.Category;
import com.kusitms.forpet.domain.Community;
import com.kusitms.forpet.domain.User;
import com.kusitms.forpet.dto.ApiResponse;
import com.kusitms.forpet.dto.CommunityDto;
import com.kusitms.forpet.dto.ReviewRequestDto;
import com.kusitms.forpet.dto.placeDto;
import com.kusitms.forpet.security.TokenProvider;
import com.kusitms.forpet.service.CommunityService;
import com.kusitms.forpet.service.UserService;
import com.kusitms.forpet.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityController {
    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final CommunityService communityService;

    /**
     * 전체 게시글 조회 (동네에 해당하는 게시글만)
     */
    @GetMapping("")
    public ApiResponse getAllPostListByAddress(HttpServletRequest request) {
        String accessToken = HeaderUtil.getAccessToken(request);
        Long userId = tokenProvider.getUserIdFromToken(accessToken);

        // 사용자 주소를 가져오기
        User user = userService.findByUserId(userId);
        String[] addressList = user.getAddress().split("#");

        // 인기 7개(id, 카테고리, 글 제목, 좋아요수, 댓글수)
        // 모임 7개(id, 글 제목, 좋아요수, 댓글수)
        // 나눔 9개 (id, 사진, 글 제목)
        // 자랑 9개 (id, 사진, 글 제목)

        Map<String, List<CommunityDto.CommunityResponse>> postList = new HashMap<>();

        List<Community> popularList = communityService.findOrderByThumbsUpAndAddress(addressList);
        popularList = popularList.subList(0, 7); // 7개

        List<Community> meetingList = communityService.findByCategoryAndAddress(Category.MEETING, addressList);
        meetingList = meetingList.subList(0, 7); // 7개

        List<Community> sharingList = communityService.findByCategoryAndAddress(Category.SHARING, addressList);
        sharingList = sharingList.subList(0, 9); // 9개

        List<Community> boastingList = communityService.findByCategoryAndAddress(Category.BOASTING, addressList);
        boastingList = boastingList.subList(0, 9); // 9개

        // domain -> dto
        List<CommunityDto.CommunityResponse> popularResponseList = popularList.stream()
                .map(m -> new CommunityDto.CommunityResponse(m.getPostId(), m.getUserId().getUserId(), m.getTitle(), m.getContent(),m.getDate(), m.getThumbsUpCnt(), m.getImageUrlList(), m.getCategory().getValue()))
                .collect(Collectors.toList());

        List<CommunityDto.CommunityResponse> meetingResponseList = meetingList.stream()
                .map(m -> new CommunityDto.CommunityResponse(m.getPostId(), m.getUserId().getUserId(), m.getTitle(), m.getContent(),m.getDate(), m.getThumbsUpCnt(), m.getImageUrlList(), m.getCategory().getValue()))
                .collect(Collectors.toList());

        List<CommunityDto.CommunityResponse> sharingResponseList = sharingList.stream()
                .map(m -> new CommunityDto.CommunityResponse(m.getPostId(), m.getUserId().getUserId(), m.getTitle(), m.getContent(),m.getDate(), m.getThumbsUpCnt(), m.getImageUrlList(), m.getCategory().getValue()))
                .collect(Collectors.toList());

        List<CommunityDto.CommunityResponse> boastingResponseList = boastingList.stream()
                .map(m -> new CommunityDto.CommunityResponse(m.getPostId(), m.getUserId().getUserId(), m.getTitle(), m.getContent(),m.getDate(), m.getThumbsUpCnt(), m.getImageUrlList(), m.getCategory().getValue()))
                .collect(Collectors.toList());

        postList.put("popular", popularResponseList);
        postList.put("meeting", meetingResponseList);
        postList.put("sharing", sharingResponseList);
        postList.put("boasting", boastingResponseList);

        return ApiResponse.success("data", postList);
    }

    /**
     * 검색
     */
    @GetMapping("/search")
    public ApiResponse search(HttpServletRequest request,
                              @RequestParam String keyword,
                              @RequestParam int page,
                              @RequestParam int size) {
        String accessToken = HeaderUtil.getAccessToken(request);
        Long userId = tokenProvider.getUserIdFromToken(accessToken);

        // 사용자 주소를 가져오기
        User user = userService.findByUserId(userId);
        String[] addressList = user.getAddress().split("#");

        // 페이지네이션
        List<Community> searchList = communityService.findByKeyword(keyword, addressList, page, size);

        List<CommunityDto.CommunityResponse> searchResponseList = searchList.stream()
                .map(m -> new CommunityDto.CommunityResponse(m.getPostId(), m.getUserId().getUserId(), m.getTitle(), m.getContent(),m.getDate(), m.getThumbsUpCnt(), m.getImageUrlList(), m.getCategory().getValue()))
                .collect(Collectors.toList());

        return ApiResponse.success("data", searchResponseList);
    }

    /**
     * 카테고리 리스트
     */
    @GetMapping("/list")
    public ApiResponse getAllPostListByCategory(HttpServletRequest request,
                                                @RequestParam(value="category") Category category,
                                                @RequestParam int page,
                                                @RequestParam int size) {
        String accessToken = HeaderUtil.getAccessToken(request);
        Long userId = tokenProvider.getUserIdFromToken(accessToken);

        // 사용자 주소를 가져오기
        User user = userService.findByUserId(userId);
        String[] addressList = user.getAddress().split("#");

        //System.out.println(Category.valueOf(category));
        List<Community> categoryList = communityService.findByCategoryAndAddress(category, addressList, page, size);

        // domain -> dto
        List<CommunityDto.CommunityResponse> searchResponseList = categoryList.stream()
                .map(m -> new CommunityDto.CommunityResponse(m.getPostId(), m.getUserId().getUserId(), m.getTitle(), m.getContent(),m.getDate(), m.getThumbsUpCnt(), m.getImageUrlList(), m.getCategory().getValue()))
                .collect(Collectors.toList());

        return ApiResponse.success("data", searchResponseList);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ApiResponse getPostById(@PathVariable Long postId) {
        Community community = communityService.findCommunityById(postId);
        CommunityDto.CommunityResponse communityResponse = new CommunityDto.CommunityResponse(
                community.getPostId(), community.getUserId().getUserId(), community.getTitle(), community.getContent(), community.getDate(), community.getThumbsUpCnt(), community.getImageUrlList(), community.getCategory().getValue());
        return ApiResponse.success("data", communityResponse);
    }


    /**
     * 게시글 수정
     */
    @PutMapping("{postId}")
    public ApiResponse updatePost(HttpServletRequest request,
                                  @PathVariable(value="postId") Long postId,
                                  @RequestPart(value = "community_request") CommunityDto.CommunityRequest requestDto,
                                  @RequestPart(value = "imageList") List<MultipartFile> multipartFile) {
        String accessToken = HeaderUtil.getAccessToken(request);
        Long userId = tokenProvider.getUserIdFromToken(accessToken);

        // 글쓴이의 주소를 가져오기
        User user = userService.findByUserId(userId);

        Long id = communityService.updatePost(postId, user, requestDto, multipartFile);

        return ApiResponse.updated("post_id", id);
    }
    /**
     * 게시글 삭제
     */
    @DeleteMapping("{postId}")
    public ApiResponse deletePost(@PathVariable(value="postId") Long postId) {
        return ApiResponse.success("post_id", communityService.deletePost(postId));

    }

    /**
    * 게시글 등록
     */
    @PostMapping("")
    public ApiResponse createPost(HttpServletRequest request,
                                  @RequestPart(value = "community_request") CommunityDto.CommunityRequest requestDto,
                                  @RequestPart(value = "imageList") List<MultipartFile> multipartFile) {
        String accessToken = HeaderUtil.getAccessToken(request);
        Long userId = tokenProvider.getUserIdFromToken(accessToken);

        // 글쓴이의 주소를 가져오기
        User user = userService.findByUserId(userId);

        Long id = communityService.createPost(user, requestDto, multipartFile);

        return ApiResponse.created("post_id", id);
    }

}
