import { TaskStatus } from './task-status.model';
import { TaskPriority } from './task-priority.model';

export interface TaskUpdateCommandModel {
  title: string;
  description?: string;
  priority: TaskPriority;
  status: TaskStatus;
  dueDate?: string;
  assigneeUsername?: string;
}
