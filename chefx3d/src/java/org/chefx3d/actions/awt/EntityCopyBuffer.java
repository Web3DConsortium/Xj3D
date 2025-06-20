/*
Copyright (c) 2007 Yumetech.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.MovesInstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package org.chefx3d.actions.awt;

// Standard library imports
// None

// Application specific imports
import org.chefx3d.model.Entity;

/**
 * Defines the requirements for a copy/paste buffer for transfering
 * an Entity
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface EntityCopyBuffer {
	
	/**
	 * Return whether the buffer contains an Entity.
	 *
	 * @return true if the buffer contains an Entity, false otherwise.
	 */
	public boolean hasEntity();

	/**
	 * Return the Entity in the buffer.
	 *
	 * @return The Entity that has been copied. If no Entity is in the
	 * buffer, null is returned.
	 */
	public Entity getEntity();
	
	/**
	 * Set an Entity into the buffer.
	 *
	 * @param entity The Entity to place in the buffer. If null, the
	 * buffer is cleared.
	 */
	public void setEntity(Entity entity);
}
