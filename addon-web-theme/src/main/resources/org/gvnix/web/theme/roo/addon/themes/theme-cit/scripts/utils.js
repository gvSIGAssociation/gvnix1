/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010, 2011 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function displayFecha() {
  var today = new Date();
  var day   = today.getDate();
  var month = today.getMonth() +1;
  if (month < 10) month = "0"+month;
  var year  = today.getYear();
  var dia = today.getDay();
  if (year < 1000) {
       year += 1900; 
  }
  var fecha = (day + "/" + month + "/" + year);
  var objF = document.getElementById("fecha");
  var F_txt = document.createTextNode(fecha);
  objF.replaceChild(F_txt, objF.childNodes[0]);
}

function openWindow(url, winTitle, winParams) {
  winName = window.open(url, winTitle, winParams);
  winName.focus();
}

