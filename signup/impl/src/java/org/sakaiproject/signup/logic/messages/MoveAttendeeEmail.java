/**
 * Copyright (c) 2007-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.logic.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupTrackingItem;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * This class is used by organizer of an event/meeting to notify attendee that
 * his appointment has been changed
 * </p>
 */
public class MoveAttendeeEmail extends TransferEmailBase {

	private final User organizer;

	private final User attendee;

	private final SignupTrackingItem item;

	private String emailReturnSiteId;
	
	private List<SignupTimeslot> removed;
	private List<SignupTimeslot> added;


	/**
	 * constructor
	 * 
	 * @param organizer
	 *            an User, who organizes the event/meeting
	 * @param attendee
	 *            an User, whose appointment has been changed
	 * @param item
	 *            a SignupTrackingItem object
	 * @param meeting
	 *            a SignupMeeting object
	 * @param sakaiFacade
	 *            a SakaiFacade object
	 */
	public MoveAttendeeEmail(User organizer, User attendee, SignupTrackingItem item, SignupMeeting meeting,
			SakaiFacade sakaiFacade) {
		this.organizer = organizer;
		this.attendee = attendee;
		this.item = item;
		this.meeting = meeting;
		this.setSakaiFacade(sakaiFacade);
		this.emailReturnSiteId = item.getAttendee().getSignupSiteId();
		
		removed = item.getRemovedFromTimeslot();
		added = Collections.singletonList(item.getAddToTimeslot());
		
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getHeader() {
		List<String> rv = new ArrayList<String>();
		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html; charset=UTF-8");
		rv.add("Subject: " + getSubject());
		rv.add("From: " + getFromAddress());
		rv.add("To: " + attendee.getEmail());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage() {

		StringBuilder message = new StringBuilder();
		message.append(MessageFormat.format(rb.getString("body.top.greeting.part"),
				new Object[] { makeFirstCapLetter(attendee.getDisplayName()) }));

		Object[] params = new Object[] { makeFirstCapLetter(organizer.getDisplayName()),
				"'" + meeting.getTitle() + "'",
				getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalDate(),
				getSiteTitleWithQuote(this.emailReturnSiteId), getServiceName() };
		message.append(newline + newline
				+ MessageFormat.format(rb.getString("body.organizer.change.appointment.part"), params));

		if (!meeting.isMeetingCrossDays()) {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
					getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(), newline + space };
			message.append(newline
					+ newline
					+ space
					+ MessageFormat
							.format(rb.getString("body.organizer.change.appointment.timeframe"), paramsTimeframe));
		} else {
			Object[] paramsTimeframe = new Object[] {
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalShortDate(),
					getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalTime(),
					getTime(item.getRemovedFromTimeslot().get(0).getEndTime()).toStringLocalShortDate(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getStartTime()).toStringLocalShortDate(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalTime(),
					getTime(item.getAddToTimeslot().getEndTime()).toStringLocalShortDate(), newline + space };
			message.append(newline
					+ newline
					+ space
					+ MessageFormat.format(rb.getString("body.organizer.change.appointment.crossdays.timeframe"),
							paramsTimeframe));
		}

		message.append(newline
				+ newline
				+ MessageFormat.format(rb.getString("body.attendeeCheck.meetingStatus.B"),
						new Object[] { getServiceName() }));
		/* footer */
		message.append(newline + getFooter(newline, this.emailReturnSiteId));

		return message.toString();
	}
	
	@Override
	public String getFromAddress() {
		return StringUtils.defaultIfEmpty(organizer.getEmail(), getServerFromAddress());
	}
	
	@Override
	public String getSubject() {
		return MessageFormat.format(rb.getString("subject.organizer.change.appointment.field"), new Object[] {
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalDate(),
					getTime(item.getRemovedFromTimeslot().get(0).getStartTime()).toStringLocalTime(),
					getAbbreviatedMeetingTitle() });
	}

	@Override
	public List<SignupTimeslot> getRemoved() {
		return removed;
	}

	@Override
	public List<SignupTimeslot> getAdded() {
		return added;
	}

}
