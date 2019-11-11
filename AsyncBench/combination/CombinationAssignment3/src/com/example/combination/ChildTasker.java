package com.example.combination;

public class ChildTasker extends Tasker {

	public ChildTasker() {
		super();
	}

	public void start() {
		execute("");
	}

	@Override
	protected String doInBackground(String... params) {
		StringBuffer sb = new StringBuffer();
		for (String param : params) {
			if (isCancelled()) {
				break;
			}
			sb.append(param);
		}
		return sb.toString();
	}
}
