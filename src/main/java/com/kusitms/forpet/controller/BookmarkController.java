package com.kusitms.forpet.controller;

import com.kusitms.forpet.domain.Bookmark;
import com.kusitms.forpet.repository.BookmarkRepository;
import com.kusitms.forpet.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final BookmarkRepository bookmarkRepository;

    //북마크 생성
    @PostMapping("/offline-map/{placeid}/bookmark")
    public Long createBookMark(@PathVariable("placeid") Long placeid) {
        //회원정보


        Long id = bookmarkService.createBookMark(placeid);

        return id;

    }

    //카테고리별 북마크
    @GetMapping("offline-map/bookmark/category")
    public List<Bookmark> showBookmarkByCategory(@RequestParam String category) {
        return bookmarkRepository.find(category);
    }

    /*
    //회원별 북마크 리스트
    @GetMapping("/offline-map/{memberid}/bookmark")
    public List<Bookmark> showBookMark(@PathVariable("memberid") Long memberid) {
        return bookmarkRepository.findAllById(Collections.singleton(memberid));
    }
    */


}