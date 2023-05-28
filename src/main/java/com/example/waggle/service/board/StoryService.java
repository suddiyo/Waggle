package com.example.waggle.service.board;

import com.example.waggle.domain.board.Media;
import com.example.waggle.domain.board.Story;
import com.example.waggle.domain.board.comment.Comment;
import com.example.waggle.domain.board.comment.Reply;
import com.example.waggle.domain.board.hashtag.BoardHashtag;
import com.example.waggle.domain.board.hashtag.Hashtag;
import com.example.waggle.domain.member.Member;
import com.example.waggle.dto.board.*;
import com.example.waggle.dto.member.MemberDto;
import com.example.waggle.repository.MemberRepository;
import com.example.waggle.repository.board.HashtagRepository;
import com.example.waggle.repository.board.MediaRepository;
import com.example.waggle.repository.board.boardtype.StoryRepository;
import com.example.waggle.repository.board.comment.CommentRepository;
import com.example.waggle.repository.board.comment.ReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StoryService {

    private final StoryRepository storyRepository;
    private final MemberRepository memberRepository;
    private final HashtagRepository hashtagRepository;
    private final MediaRepository mediaRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    /**
     * 조회는 entity -> dto과정을,
     * 저장은 dto -> entity 과정을 거치도록 한다.(기본)
     */

    //1. ===========조회===========

    //1.1 그룹 조회

    //1.1.1 전체 조회 -> 후에 시간 순으로 조회
    //P1. 지금은 story -> storySimpleDto로 변경하지만 조회를 dto로 변경하면 query양이 적어질 것이다.
    //P2. paging 필수
    @Transactional(readOnly = true)
    public List<StorySimpleDto> findAllStory() {
        List<Story> allStory = storyRepository.findAll();
        List<StorySimpleDto> simpleStories = new ArrayList<>();
        for (Story story : allStory) {
            simpleStories.add(StorySimpleDto.toDto(story));
        }

        return  simpleStories;
    }

    //1.1.2 회원 정보에 따른 전체 조회
    @Transactional(readOnly = true)
    public List<StorySimpleDto> findAllStoryByMember(String username) {
        Optional<Member> MemberByUsername = memberRepository.findByUsername(username);
        if (MemberByUsername.isEmpty()) {
            log.info("can't find user!");
            // error message 출력
        }
        List<Story> storyByUsername = storyRepository.findByUsername(username);
        List<StorySimpleDto> simpleStories = new ArrayList<>();
        for (Story story : storyByUsername) {
            simpleStories.add(StorySimpleDto.toDto(story));
        }

        return simpleStories;
    }

    //1.2 낱개 조회
    @Transactional(readOnly = true)
    public StoryDto findStoryByBoardId(Long id) {
        Optional<Story> storyByBoardId = storyRepository.findByBoardId(id);
        if (storyByBoardId.isEmpty()) {
            //error and return null
        }
        return StoryDto.toDto(storyByBoardId.get());
    }

    //2. ===========저장===========

    //2.1 story 저장(media, hashtag 포함)
    //***중요!
    public void saveStory(StoryDto saveStoryDto) {
        Story saveStory = saveStoryDto.toEntity();
        storyRepository.save(saveStory);
        Optional<Member> byUsername = memberRepository.findByUsername(saveStoryDto.getUsername());
        if (byUsername.isEmpty()) {
            //error message
        }
        //hashtag 저장
        if(!saveStoryDto.getHashtags().isEmpty()){
            for (String hashtagContent : saveStoryDto.getHashtags()) {
                saveHashtag(saveStory, hashtagContent);
            }
        }
        //media 저장
        if (!saveStoryDto.getMedias().isEmpty()) {
            for (String mediaURL : saveStoryDto.getMedias()) {
                Media buildMedia = Media.builder().url(mediaURL).board(saveStory).build();
                mediaRepository.save(buildMedia);
            }
        }
    }

    //2.2 story_comment 저장
    //아직 순서 관련 메서드를 작성하지 않았다.
    public void saveComment(CommentDto commentDto, StoryDto storyDto, MemberDto memberDto) {
        Optional<Story> storyByBoardId = storyRepository.findByBoardId(storyDto.getId());
        Optional<Member> memberByUsername = memberRepository.findByUsername(memberDto.getUsername());

        if (storyByBoardId.isPresent() && memberByUsername.isPresent()) {
            Comment buildComment = Comment.builder()
                    .content(commentDto.getContent())
                    .board(storyByBoardId.get())
                    .member(memberByUsername.get())
                    .build();
            commentRepository.save(buildComment);
        }

    }

    //2.3 story_comment_reply 저장
    public void saveReply(ReplyDto replyDto, CommentDto commentDto, MemberDto memberDto) {
        Optional<Comment> commentById = commentRepository.findById(commentDto.getId());
        Optional<Member> memberByUsername = memberRepository.findByUsername(memberDto.getUsername());

        if (commentById.isPresent() && memberByUsername.isPresent()) {
            Reply buildReply = Reply.builder()
                    .content(replyDto.getContent())
                    .comment(commentById.get())
                    .member(memberByUsername.get())
                    .build();

            replyRepository.save(buildReply);
        }
    }

    //3. ===========수정===========

    //3.1 story 수정(media, hashtag 포함)
    public void changeStory(Long BoardId, StoryDto storyDto) {
        Optional<Story> storyByBoardId = storyRepository.findByBoardId(BoardId);
        if (storyByBoardId.isPresent()) {
            storyByBoardId.get().changeStory(storyDto.getContent(),storyDto.getThumbnail());

            //delete(media)
            for (Media media : storyByBoardId.get().getMedias()) {
                mediaRepository.delete(media);
            }

            //newly insert data(media)
            for (String media : storyDto.getMedias()) {
                Media board = Media.builder().url(media).board(storyByBoardId.get()).build();
                mediaRepository.save(board);
            }

            //delete connecting relate (boardHashtag)
            for (BoardHashtag boardHashtag : storyByBoardId.get().getBoardHashtags()) {
                boardHashtag.cancelHashtag();
            }

            //newly insert data(hashtag, boardHashtag)
            for (String hashtag : storyDto.getHashtags()) {
                saveHashtag(storyByBoardId.get(), hashtag);
            }
        }

    }

    /**
     * private method
     * use at : 2.1, 3.1
     * @param story
     * @param hashtag
     */
    private void saveHashtag(Story story, String hashtag) {
        Optional<Hashtag> hashtagByContent = hashtagRepository.findByTag(hashtag);
        if (hashtagByContent.isEmpty()) {
            Hashtag buildHashtag = Hashtag.builder().tag(hashtag).build();
            hashtagRepository.save(buildHashtag);
            BoardHashtag buildBoardHashtag = BoardHashtag.builder()
                    .hashtag(buildHashtag).board(story).build();
        }//아래 else가 좀 반복되는 것 같다...
        else{
            BoardHashtag buildBoardHashtag = BoardHashtag.builder()
                    .hashtag(hashtagByContent.get()).board(story).build();
        }
    }

    //3.2 story_comment 수정
    public void changeComment(CommentDto commentDto) {
        Optional<Comment> commentById = commentRepository.findById(commentDto.getId());
        if (!commentById.isEmpty()) {
            commentById.get().changeContent(commentDto.getContent());
        }
    }

    //3.3 story_comment_reply 수정
    public void changeReply(ReplyDto replyDto) {
        Optional<Reply> replyById = replyRepository.findById(replyDto.getId());
        if (!replyById.isEmpty()) {
            replyById.get().changeContent(replyDto.getContent());
        }
    }

    //4. ===========삭제(취소)===========

    //4.1 story 삭제
    // (media, hashtag 포함)
    public void removeStory(StoryDto storyDto) {
        Optional<Story> storyByBoardId = storyRepository.findByBoardId(storyDto.getId());
        // solution 1
//        for (BoardHashtag boardHashtag : storyByBoardId.getBoardHashtags()) {
//            boardHashtag.cancelHashtag();
//        }
        // solution 2
        if (storyByBoardId.isPresent()) {
            storyRepository.delete(storyByBoardId.get());
        }
    }

    //4.2 story_comment 저장
    public void removeComment(CommentDto commentDto) {
        Optional<Comment> commentById = commentRepository.findById(commentDto.getId());
        if (!commentById.isEmpty()) {
            commentRepository.delete(commentById.get());
        }
    }

    //4.3 story_comment_reply 저장
    public void removeReply(ReplyDto replyDto) {
        Optional<Reply> replyById = replyRepository.findById(replyDto.getId());
        if (!replyById.isEmpty()) {
            replyRepository.delete(replyById.get());
        }
    }
}