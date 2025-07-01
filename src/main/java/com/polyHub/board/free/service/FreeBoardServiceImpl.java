package com.polyHub.board.free.service;

import com.polyHub.board.free.dto.*;
import com.polyHub.board.free.entity.FreeBoardComment;
import com.polyHub.board.free.entity.FreeBoardPost;
import com.polyHub.board.free.entity.PostReaction;
import com.polyHub.board.free.entity.ReactionType;
import com.polyHub.board.free.repository.FreeBoardCommentRepository;
import com.polyHub.board.free.repository.FreeBoardPostRepository;
import com.polyHub.board.free.repository.PostReactionRepository;
import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreeBoardServiceImpl implements FreeBoardService {

    private final FreeBoardPostRepository postRepository;
    private final PostReactionRepository reactionRepository;
    private final FreeBoardCommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public ReactionDto reactToPost(Long postId, Long memberId, ReactionType reactionType) {
        FreeBoardPost post = postRepository.findById(postId).orElseThrow();
        Optional<PostReaction> existingReactionOpt = reactionRepository.findByMemberIdAndPostId(memberId, postId);

        if (existingReactionOpt.isPresent()) {
            PostReaction existingReaction = existingReactionOpt.get();
            // 이미 같은 반응을 했다면 -> 취소
            if (existingReaction.getReactionType() == reactionType) {
                reactionRepository.delete(existingReaction);
                updatePostReactionCount(post, reactionType, -1);
            } else { // 다른 반응을 했다면 -> 변경
                updatePostReactionCount(post, existingReaction.getReactionType(), -1); // 기존 반응 취소
                existingReaction.setReactionType(reactionType); // 새 반응으로 변경
                reactionRepository.save(existingReaction);
                updatePostReactionCount(post, reactionType, 1); // 새 반응 추가
            }
        } else { // 처음 반응한다면 -> 추가
            Member member = Member.builder().id(memberId).build(); // 임시 Member 객체
            PostReaction newReaction = PostReaction.builder()
                    .member(member).post(post).reactionType(reactionType).build();
            reactionRepository.save(newReaction);
            updatePostReactionCount(post, reactionType, 1);
        }

        FreeBoardPost updatedPost = postRepository.save(post);
        String userReaction = reactionRepository.findByMemberIdAndPostId(memberId, postId)
                .map(r -> r.getReactionType().name())
                .orElse("NONE");

        ReactionDto dto = new ReactionDto();
        dto.setLikeCount(updatedPost.getLikeCount());
        dto.setDislikeCount(updatedPost.getDislikeCount());
        dto.setUserReaction(userReaction);
        return dto;
    }

    /**
     * [구현] 게시글 목록을 조회합니다.
     */
    @Override
    public Page<FreeBoardListDto> findPosts(Pageable pageable) {
        Page<FreeBoardPost> posts = postRepository.findAll(pageable);
        return posts.map(this::convertToBoardListDto); // Page 객체를 DTO 페이지로 변환
    }

    private void updatePostReactionCount(FreeBoardPost post, ReactionType reactionType, int amount) {
        if (reactionType == ReactionType.LIKE) {
            post.setLikeCount(post.getLikeCount() + amount);
        } else {
            post.setDislikeCount(post.getDislikeCount() + amount);
        }
    }

    /**
     * [수정] 게시글 상세 조회 시 댓글과 답글을 계층 구조로 함께 조회합니다.
     */
    @Override
    @Transactional
    public FreeBoardDetailDto findPostById(Long postId, Long memberId) {
        FreeBoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.setViewCount(post.getViewCount() + 1);

        List<FreeBoardComment> allComments = commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);

        // 부모 ID를 기준으로 답글들을 그룹핑합니다.
        Map<Long, List<CommentDto>> replyMap = allComments.stream()
                // [수정] getParent()를 사용하여 부모 댓글의 존재 여부를 확인합니다.
                .filter(comment -> comment.getParent() != null)
                .map(this::convertToCommentDto)
                // [수정] CommentDto의 parentId를 기준으로 그룹핑합니다. (이 부분은 DTO를 사용하므로 기존 로직 유지)
                .collect(Collectors.groupingBy(CommentDto::getParentId));

        // 원댓글만 필터링하여 최종적인 계층 구조를 만듭니다.
        List<CommentDto> commentDtos = allComments.stream()
                // [수정] getParent()를 사용하여 최상위 댓글(부모가 없는)만 필터링합니다.
                .filter(comment -> comment.getParent() == null)
                .map(comment -> {
                    CommentDto parentDto = convertToCommentDto(comment);
                    // DTO로 변환된 원댓글의 ID를 사용해 답글 목록을 찾아서 설정합니다.
                    parentDto.setReplies(replyMap.getOrDefault(comment.getId(), new ArrayList<>()));
                    return parentDto;
                })
                .collect(Collectors.toList());

        String userReaction = reactionRepository.findByMemberIdAndPostId(memberId, postId)
                .map(r -> r.getReactionType().name())
                .orElse("NONE");

        return FreeBoardDetailDto.builder()
                .id(post.getId()).title(post.getTitle()).content(post.getContent())
                .writer(post.getMember().getName()).createdAt(post.getCreatedAt())
                .viewCount(post.getViewCount()).likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount()).userReaction(userReaction)
                .comments(commentDtos)
                .build();
    }

    /**
     * [구현] 새 게시글을 작성합니다.
     */
    @Override
    @Transactional
    public Long writePost(FreeBoardWriteDto writeDto, Long memberId) {
        Member writer = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        FreeBoardPost newPost = FreeBoardPost.builder()
                .title(writeDto.getTitle())
                .content(writeDto.getContent())
                .member(writer)
                .build();

        FreeBoardPost savedPost = postRepository.save(newPost);
        return savedPost.getId();
    }


    /**
     * [수정] 댓글 Entity -> DTO 변환 시 parentId도 함께 변환합니다.
     */
    private CommentDto convertToCommentDto(FreeBoardComment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writer(comment.getMember().getName())
                .createdAt(comment.getCreatedAt())
                //.parentId(comment.getParentId()) // parentId 추가
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .build();
    }

    // Entity를 목록용 DTO로 변환하는 헬퍼 메소드
    private FreeBoardListDto convertToBoardListDto(FreeBoardPost post) {
        return FreeBoardListDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .writer(post.getMember().getName()) // Member 객체에서 이름 가져오기
                .createdAt(post.getCreatedAt())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getComments().size()) // 댓글 리스트의 크기로 댓글 수 계산
                .build();
    }

    /**
     * [수정] 댓글/답글 작성 로직
     */
    @Override
    @Transactional
    public CommentDto writeComment(CommentWriteDto writeDto, Long memberId) {
        Member writer = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));
        FreeBoardPost post = postRepository.findById(writeDto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        FreeBoardComment parentComment = null;
        // DTO에 parentId가 포함되어 있다면(즉, 답글이라면)
        if (writeDto.getParentId() != null) {
            // 부모 댓글 엔티티를 DB에서 조회합니다.
            parentComment = commentRepository.findById(writeDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }

        // 엔티티를 생성합니다.
        FreeBoardComment comment = FreeBoardComment.builder()
                .post(post)
                .member(writer)
                .parent(parentComment) // [수정] parentId(Long) 대신 parent(객체)를 설정합니다.
                .content(writeDto.getContent())
                .build();

        FreeBoardComment savedComment = commentRepository.save(comment);

        // 방금 저장한 엔티티를 다시 DTO로 변환하여 반환
        return convertToCommentDto(savedComment);
    }

    /**
     * [추가] 게시글 수정 로직
     */
    @Override
    @Transactional
    public void updatePost(Long postId, FreeBoardWriteDto updateDto, Long memberId) {
        FreeBoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("수정할 권한이 없습니다.");
        }

        // 제목과 내용 업데이트
        post.setTitle(updateDto.getTitle());
        post.setContent(updateDto.getContent());
    }

    /**
     * [추가] 게시글 삭제 로직
     */
    @Override
    @Transactional
    public void deletePost(Long postId, Long memberId) {
        FreeBoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("삭제할 권한이 없습니다.");
        }

        // DB에서 게시글 삭제 (연관된 댓글, 추천 등도 cascade 옵션으로 함께 삭제됨)
        postRepository.delete(post);
    }

    /**
     * [수정] 게시글 목록 조회 시 검색 기능을 구현합니다.
     */
    @Override
    public Page<FreeBoardListDto> findPosts(Pageable pageable, String keyword) {
        Page<FreeBoardPost> posts;
        // StringUtils.hasText()는 keyword가 null이거나, 비어있거나, 공백만 있는 경우 false를 반환합니다.
        if (StringUtils.hasText(keyword)) {
            // 검색어가 있으면 제목 또는 내용에서 검색
            posts = postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        } else {
            // 검색어가 없으면 전체 목록 조회
            posts = postRepository.findAll(pageable);
        }
        return posts.map(this::convertToBoardListDto);
    }

}
