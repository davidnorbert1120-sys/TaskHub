import { UserItemModel } from './user-item.model';

export interface AuthResponseModel {
  token: string;
  user: UserItemModel;
}
