export type MemberRole = 'OWNER' | 'MEMBER';

export interface ProjectMemberItemModel {
  id: number;
  username: string;
  role: MemberRole;
}
