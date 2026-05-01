import { TaskStatus } from './task-status.model';

export interface UpdateTaskStatusCommandModel {
  status: TaskStatus;
}
