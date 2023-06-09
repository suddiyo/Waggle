package com.example.waggle.dto.board;

import com.example.waggle.domain.board.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryDto {

    private Long id;
    private String content;
    private String username;
    private int recommend;
    private String thumbnail;
    private LocalDateTime createDate;
    private int like;

    @Builder.Default
    private List<String> hashtags = new ArrayList<>();
    @Builder.Default
    private List<String> medias = new ArrayList<>();
    @Builder.Default
    private List<CommentDto> comments = new ArrayList<>();

    //스토리 하나를 조회할 때 사용
    static public StoryDto toDto(Story story) {
        return StoryDto.builder()
                .id(story.getId())
                .content(story.getContent())
                .username(story.getMember().getUsername())
                .thumbnail(story.getThumbnail())
                .createDate(story.getCreatedDate())
                .like(story.getLikes().size())
                .hashtags(story.getBoardHashtags().stream()
                        .map(bh -> bh.getHashtag().getTag()).collect(Collectors.toList()))
                .medias(story.getMedias().stream()
                        .map(m -> m.getUrl()).collect(Collectors.toList()))
                .comments(story.getComments().stream()
                        .map(c -> CommentDto.toDto(c)).collect(Collectors.toList()))
                .build();
    }
    //스토리 하나를 저장할 때 사용
    //스토리 안의 다른 엔티티는 개별로 저장, 하지만 메서드를 통해 연관관계를 맺어준다.
    public Story toEntity() {
        return Story.builder()
                .content(content)
                .thumbnail(thumbnail)
                .build();
    }
}
