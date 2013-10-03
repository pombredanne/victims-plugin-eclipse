package com.redhat.victims.plugin.eclipse;

import com.redhat.victims.VictimsException;

/*
 * #%L
 * This file is part of victims-plugin-ant.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

/**
 * Almost redundant exception class.
 * TODO: This should probably extend
 * org.eclipse.core.commands.ExecutionException and it should
 * be used to exit the program upon finding vulnerabilities/errors.
 * 
 * @author kurt
 *
 */
public class VictimsBuildException extends VictimsException {

	private static final long serialVersionUID = -3411399027684620634L;

	VictimsBuildException(String err){
		super(err);
	}
	
	VictimsBuildException(String str, Throwable cause){
		super(str, cause);
	}
}