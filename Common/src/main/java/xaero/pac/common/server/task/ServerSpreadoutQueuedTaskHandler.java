/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2022, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.task;

import xaero.pac.common.server.IServerData;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;

public final class ServerSpreadoutQueuedTaskHandler<T extends IServerSpreadoutTask<T>> extends ServerSpreadoutTaskHandler<T, T> {
	//task and task holder are the same in this case

	private final Deque<T> taskQueue;

	private ServerSpreadoutQueuedTaskHandler(Deque<T> tasks, Function<T, T> holderToTask, int perTickLimit, int perTickPerTaskLimit) {
		super(holderToTask, perTickLimit, perTickPerTaskLimit);
		this.taskQueue = tasks;
	}

	@Override
	protected Iterator<T> getTaskHolderIterator(IServerData<?, ?> serverData) {
		return taskQueue.iterator();
	}

	public static final class Builder<T extends IServerSpreadoutTask<T>> extends ServerSpreadoutTaskHandler.Builder<T, T, Builder<T>> {

		private Builder(){}

		@Override
		public Builder<T> setDefault() {
			super.setDefault();
			return self;
		}

		@Override
		public Builder<T> setHolderToTask(Function<T, T> holderToTask) {
			if(holderToTask != null)
				throw new RuntimeException(new IllegalAccessException());
			return super.setHolderToTask(holderToTask);
		}

		@Override
		protected ServerSpreadoutTaskHandler<T, T> buildInternally() {
			return new ServerSpreadoutQueuedTaskHandler<>(new LinkedList<>(), Function.<T>identity(), perTickLimit, perTickPerTaskLimit);
		}

		public static <T extends IServerSpreadoutTask<T>> Builder<T> begin(){
			return new Builder<T>().setDefault();
		}

	}

}
