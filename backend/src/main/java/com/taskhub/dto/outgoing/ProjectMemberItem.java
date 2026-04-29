package com.taskhub.dto.outgoing;

import com.taskhub.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberItem {

    private Long id;

    private String username;

    private MemberRole role;
}
