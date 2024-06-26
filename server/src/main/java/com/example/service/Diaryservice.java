package com.example.service;

import com.example.domain.entity.DiaryEntity;
import com.example.dto.DiaryDto;
import com.example.repository.DiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Diaryservice {
    private final DiaryRepository diaryrepository;//변수에서 할당된 값이 변경될 수 없음을 나타냄

    // 의존성 주입을 통해 Diaryrepository 객체를 주입받음
    @Autowired
    public Diaryservice(DiaryRepository diaryrepository){

        this.diaryrepository =diaryrepository;
    }
    // 클라이언트로부터 받은 일기를 저장하는 메서드
    public void writeDiary(DiaryDto diaryDto){
        DiaryEntity diaryEntity = diaryDto.toEntity();

        //일기 엔티티를 데이터베이스에 저장
        diaryrepository.save(diaryEntity);
    }
}
