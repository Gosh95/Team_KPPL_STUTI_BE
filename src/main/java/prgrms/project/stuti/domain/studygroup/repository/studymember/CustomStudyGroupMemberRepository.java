package prgrms.project.stuti.domain.studygroup.repository.studymember;

import java.util.List;
import java.util.Optional;

import prgrms.project.stuti.domain.studygroup.model.StudyGroupMember;

public interface CustomStudyGroupMemberRepository {

	boolean isStudyLeader(Long memberId, Long studyGroupId);

	Optional<StudyGroupMember> findStudyGroupMemberById(Long studyGroupMemberId);

	List<StudyGroupMember> findStudyGroupMembers(Long studyGroupId);
}
