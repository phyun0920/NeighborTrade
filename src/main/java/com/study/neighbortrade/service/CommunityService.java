package com.study.neighbortrade.service;

import com.study.neighbortrade.domain.community.CommunityComment;
import com.study.neighbortrade.domain.community.CommunityPost;
import com.study.neighbortrade.domain.community.CommunityPostStatus;
import com.study.neighbortrade.domain.member.Member;
import com.study.neighbortrade.dto.community.CommunityCommentRequestDto;
import com.study.neighbortrade.dto.community.CommunityPostRequestDto;
import com.study.neighbortrade.repository.CommunityCommentRepository;
import com.study.neighbortrade.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;

    public Page<CommunityPost> list(Member viewer, int page) {
        requireLocalVerified(viewer);
        Pageable pageable = PageRequest.of(Math.max(page, 0), 15);
        return communityPostRepository.findByNeighborhoodAndStatusOrderByCreatedAtDesc(viewer.getVerifiedNeighborhood(), CommunityPostStatus.VISIBLE, pageable);
    }

    @Transactional
    public CommunityPost detail(Long id) {
        CommunityPost post = findById(id);
        if (post.getStatus() == CommunityPostStatus.HIDDEN) throw new IllegalArgumentException("숨김 처리된 글입니다.");
        post.increaseViewCount();
        return post;
    }

    public CommunityPost findById(Long id) {
        return communityPostRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("동네생활 글을 찾을 수 없습니다."));
    }

    public List<CommunityComment> comments(CommunityPost post) {
        return communityCommentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    @Transactional
    public CommunityPost create(Member author, CommunityPostRequestDto dto) {
        requireLocalVerified(author);
        return communityPostRepository.save(CommunityPost.builder()
                .author(author)
                .neighborhood(author.getVerifiedNeighborhood())
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(CommunityPostStatus.VISIBLE)
                .build());
    }

    @Transactional
    public void update(Long id, Member author, CommunityPostRequestDto dto) {
        CommunityPost post = findById(id);
        if (!post.isAuthor(author)) throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        post.update(dto.getTitle(), dto.getContent());
    }

    @Transactional
    public void addComment(Long postId, Member author, CommunityCommentRequestDto dto) {
        requireLocalVerified(author);
        CommunityPost post = findById(postId);
        communityCommentRepository.save(CommunityComment.builder()
                .post(post)
                .author(author)
                .content(dto.getContent())
                .build());
    }

    @Transactional
    public void deleteComment(Long commentId, Member member) {
        CommunityComment comment = communityCommentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!comment.isAuthor(member)) throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        communityCommentRepository.delete(comment);
    }

    @Transactional
    public void hideByAdmin(Long id) {
        findById(id).hide();
    }

    private void requireLocalVerified(Member member) {
        if (member == null || !member.isLocalVerified() || member.getVerifiedNeighborhood() == null) {
            throw new IllegalArgumentException("동네 인증 후 이용할 수 있습니다.");
        }
    }
}
